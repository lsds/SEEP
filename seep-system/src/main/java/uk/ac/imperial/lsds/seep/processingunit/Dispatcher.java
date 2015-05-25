package uk.ac.imperial.lsds.seep.processingunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.routing.IRoutingObserver;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

public class Dispatcher implements IRoutingObserver {

	//private final Map<Integer, DataTuple> senderQueues = new HashMap<Integer, ConcurrentNavigableMap<Integer, DataTuple>>();
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private static final long ROUTING_CTRL_DELAY = 1 * 1000;
	private static final long SEND_TIMEOUT = 1 * 1000;
	private final int MAX_TOTAL_QUEUE_SIZE;
	private final boolean bestEffort;
	private final boolean optimizeReplay;
	
	private final IProcessingUnit owner;
	private final Map<Integer, DispatcherWorker> workers = new HashMap<>();
	private final Set<RoutingControlWorker> rctrlWorkers = new HashSet<>();
	private final FailureCtrlHandler fctrlHandler = new FailureCtrlHandler();
	
	private ArrayList<OutputQueue> outputQueues;
	private final OperatorOutputQueue opQueue;
	//private final FailureCtrl nodeFctrl = new FailureCtrl();	//KEEP THIS
	private final FailureCtrl combinedDownFctrl = new FailureCtrl();
	private final Map<Integer, Set<Long>> downAlives = new HashMap<>();	//TODO: Concurrency?
	
	private final Map<String, Integer> idxMapper = new HashMap<String, Integer>(); //Needed for replay after conn failure
	
	private final Object lock = new Object(){};
	
	public Dispatcher(IProcessingUnit owner)
	{
		this.owner = owner;
		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		optimizeReplay = GLOBALS.valueFor("optimizeReplay").equals("true");
		
		if (owner.getOperator().getOpContext().isSource())
		{
			MAX_TOTAL_QUEUE_SIZE = Integer.parseInt(GLOBALS.valueFor("maxSrcTotalQueueSizeTuples"));
		}
		else
		{
			MAX_TOTAL_QUEUE_SIZE = Integer.parseInt(GLOBALS.valueFor("maxTotalQueueSizeTuples"));
			//MAX_TOTAL_QUEUE_SIZE = 1;
		}
		//opQueue = new OperatorOutputQueue(Integer.MAX_VALUE);
		opQueue = new OperatorOutputQueue(MAX_TOTAL_QUEUE_SIZE);

		for(int i = 0; i<owner.getOperator().getOpContext().getDeclaredWorkingAttributes().size(); i++){
			idxMapper.put(owner.getOperator().getOpContext().getDeclaredWorkingAttributes().get(i), i);
		}
	}
	
	public void setOutputQueues(ArrayList<OutputQueue> outputQueues)
	{
		//TODO: Not sure how well this will work wrt connection updates,
		//threading etc.
		this.outputQueues = outputQueues;
		
		/*
		synchronized(lock)
		{
			String srcMaxBufferMB = GLOBALS.valueFor("srcMaxBufferMB");
			if (srcMaxBufferMB != null && owner.getOperator().getOpContext().isSource())
			{
				int tupleSize = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));			
				MAX_NODE_OUT_BUFFER_TUPLES = (Integer.parseInt(srcMaxBufferMB) * 1024 * 1024) / tupleSize;
				//MAX_NODE_OUT_BUFFER_TUPLES = Integer.parseInt(srcMaxBufferMB) / tupleSize;
			}
			
		}
		*/
		
		for(int i = 0; i < outputQueues.size(); i++)
		{
			//1 thread per worker - assumes fan-out not too crazy and that we're network bound.
			DispatcherWorker worker = new DispatcherWorker(outputQueues.get(i), owner.getPUContext().getDownstreamTypeConnection().elementAt(i));			
			Thread workerT = new Thread(worker);
			workers.put(i, worker);
			workerT.start();
		}
		logger.info("Set dispatcher output queues and started dispatcher workers.");
	}
	
	public void startRoutingCtrlWorkers()
	{
		for(Integer downOpId : owner.getOperator().getOpContext().getDownstreamOpIdList())
		{
			//1 thread per worker - assumes fan-out not too crazy and that we're network bound.
			RoutingControlWorker worker = new RoutingControlWorker(downOpId);			
			Thread workerT = new Thread(worker);
			rctrlWorkers.add(worker);
			workerT.start();
		}
		
		logger.info("Started dispatcher routing control workers.");
	}
	
	public void startDispatcherMain()
	{
		//TODO: Is this safe?
		Thread mainT = new Thread(new DispatcherMain());
		mainT.start();
		logger.info("Started dispatcher main.");
	}
	
	public FailureCtrl getCombinedDownFailureCtrl()
	{
		FailureCtrl copy = null;
		synchronized(lock)
		{
			copy = new FailureCtrl(combinedDownFctrl);
		}
		return copy;
	}
	
	public void dispatch(DataTuple dt) 
	{
		if (!bestEffort)
		{
			dispatchReliable(dt); 
		}
		else
		{
			dispatchBestEffort(dt);
		}	
	}
	
	private void dispatchBestEffort(DataTuple dt)
	{
		opQueue.add(dt);
	}
	
	private void dispatchReliable(DataTuple dt) 
	{ 
		//TODO: Locking around combinedDownFctrl?
		long ts = dt.getPayload().timestamp;
		if (combinedDownFctrl.lw() >= ts || combinedDownFctrl.acks().contains(ts))
		{
			return;	//Trim
		}
		if (!(optimizeReplay && combinedDownFctrl.alives().contains(ts)))
		{
			opQueue.add(dt);
		}
		else
		{
			sharedReplayLog.add(dt);
		}
	}
	
	
	/*
	private void dispatchBestEffortOld(DataTuple dt)
	{
		ArrayList<Integer> targets = owner.getOperator().getRouter().forward_highestWeight(dt);
		synchronized(lock)
		{
			while (totalQueueSize() > MAX_TOTAL_QUEUE_SIZE || targets.isEmpty())
			{
				logger.debug("Best effort dispatcher waiting on full queues, size="+totalQueueSize());
				try
				{
					lock.wait();
				}
				catch(InterruptedException e) {}
				targets = owner.getOperator().getRouter().forward_highestWeight(dt);
			}
		}
		//Drop the lock before sending
		sendToDispatcher(dt, targets);
	}
	
	private void dispatchReliable(DataTuple dt, boolean retransmission)
	{		
		synchronized(lock)
		{
			if (nodeOutBuffer.containsKey(dt.getPayload().timestamp) && !retransmission) 
			{
				logger.info("Discarding tuple already added to node out buffer: "+dt.getPayload().timestamp);
				return; 
			}
		}
	
		ArrayList<Integer> targets = owner.getOperator().getRouter().forward_highestWeight(dt);
		synchronized(lock)
		{
			while (nodeOutBuffer.size() > MAX_NODE_OUT_BUFFER_TUPLES ||
					(!retransmission && totalQueueSize() > MAX_TOTAL_QUEUE_SIZE) ||
					targets.isEmpty())
			{
				logger.debug("Dispatcher waiting on full node out buf, size="+nodeOutBuffer.size());
				//return
				
				try
				{
					lock.wait();
				}
				catch(InterruptedException e) {}
				
				if (!retransmission && nodeOutBuffer.containsKey(dt.getPayload().timestamp)) { return; }
				//TODO: Any way to drop the lock before calling this?
				targets = owner.getOperator().getRouter().forward_highestWeight(dt);
				
			}

			
			//TODO: Flow control if total q length > max q for round robin?
			nodeOutBuffer.put(dt.getPayload().timestamp, dt);
			nodeOutTimers.put(dt.getPayload().timestamp, System.currentTimeMillis());
		}
		
		//Drop the lock before sending.
		sendToDispatcher(dt, targets);
	}
	*/
	
	public int getTotalQlen()
	{
		return opQueue.size();
	}
	
	//Should be called with lock held
	/*
	private int totalQueueSize()
	{
		int total = 0;
		for (DispatcherWorker worker : workers.values())
		{
			total += worker.queueLength();
		}
		return total;
	}
	*/
	/*
	private void sendToDispatcher(DataTuple dt, ArrayList<Integer> targets)
	{
		for(int i = 0; i<targets.size(); i++){
			int target = targets.get(i);
			EndPoint dest = owner.getPUContext().getDownstreamTypeConnection().elementAt(target);
			// REMOTE ASYNC
			if(dest instanceof AsynchronousCommunicationChannel){
				//Probably just tweak from spu.sendData
				throw new RuntimeException("TODO");
			}
			// REMOTE SYNC
			else if(dest instanceof SynchronousCommunicationChannel){
				workers.get(target).send(dt);
				//outputQueues.get(target).sendToDownstream(dt, dest);
			}
			// LOCAL
			else if(dest instanceof Operator){
				//Probably just tweak from spu.sendData
				throw new RuntimeException("TODO");
			}
		}
	}
	*/
	
	public void ack(DataTuple dt)
	{
		if (!bestEffort)
		{
			if (!owner.getOperator().getOpContext().isSink()) { throw new RuntimeException("Logic error."); }
			long ts = dt.getPayload().timestamp;
			synchronized(lock)
			{
				combinedDownFctrl.ack(ts);
			}		
		}
		

	}
	
	public FailureCtrl handleFailureCtrl(FailureCtrl fctrl, int dsOpId) 
	{
		if (bestEffort) { throw new RuntimeException("Logic error - best effort failure ctrl."); }
		fctrlHandler.handleFailureCtrl(fctrl, dsOpId);
		return new FailureCtrl(combinedDownFctrl);
		
		/*
		if (!optimizeReplay)
		{
			return new FailureCtrl(combinedDownFctrl.lw(), combinedDownFctrl.acks())
		}
		FailureCtrl toUpstream = new FailureCtrl(nodeFctrl);
		
		if (optimizeReplay)
		{
			synchronized(lock)
			{
				toUpstream.updateAlives(nodeOutBuffer.keySet());
			}
			throw new RuntimeException("TODO: Replay optimization.");
		}
		return toUpstream;
		*/ 
	}
	
	public void routingChanged()
	{
		synchronized(lock) { lock.notifyAll(); }
	}
	
	public void stop(int target) { throw new RuntimeException("TODO"); }
	
	
	public class DispatcherMain implements Runnable
	{
		public DispatcherMain() {}
		public void run()
		{
			while(true)
			{
				//iterate sending tuples to the appropriate downstreams.
				DataTuple dt = opQueue.tryPeekHead();
				ArrayList<Integer> targets = null;
				if (dt != null) 
				{ 
					targets = owner.getOperator().getRouter().forward_highestWeight(dt); 
				}
				while(dt == null || targets == null || targets.isEmpty())
				{
					synchronized(lock)
					{
						try { lock.wait(); } catch (InterruptedException e) {}
					}
					dt = opQueue.tryPeekHead();
					if (dt != null) { targets = owner.getOperator().getRouter().forward_highestWeight(dt); }
				}
				logger.debug("Sending tuple "+dt.getPayload().timestamp +" to "+targets.get(0));
				
				
				//TODO: Do this earlier to keep op queue shorter
				//1) if acked or alive
				//		if acked remove and discard
				//		else move to shared replay log
				//
				//		continue
				//    else, as below
				FailureCtrl fctrl = getCombinedDownFailureCtrl();
				long ts = dt.getPayload().timestamp;
				if (fctrl.lw() >= ts || fctrl.acks().contains(ts) || fctrl.alives().contains(ts))
				{
					dt = opQueue.remove(dt.getPayload().timestamp);
					if (fctrl.alives().contains(ts) && dt != null)
					{
						sharedReplayLog.add(dt);
					}
					continue;
				}
				
				//Option 1: Don't remove unless there is space in target
				//Problem: Will just end up busy spinning
				//Could have disp workers signal lock when there is space (i.e. get rid of arrayblocking queue).
				//Option 2: Iterate over targets in priority order
				//Problem: Could still end up with problem 1.
				//Option 3: Try to send to blocked queue, with no timeout.
				//Problem: If routing changes the whole dispatcher will be blocked
				//Solution: Could wait on a notify and check the head of line or routing hasn't changed.
				//Essentially repeating the above.
				//Option 4: Send to blocking queue with a timeout.
				//Problem: A bit crude. Can't check if the tuple has been acked, routing has changed or a 
				//higher priority tuple has been queued. Simplest for now though perhaps. More importantly
				//could hurt parallelism? Can perhaps avoid with priorities. 
				boolean success = workers.get(targets.get(0)).trySend(dt, SEND_TIMEOUT);
				if (success)
				{
					dt = opQueue.remove(dt.getPayload().timestamp);
					/*
					if (dt != null)
					{
						if (!bestEffort) { 
							logger.error("TODO: Add to node out buf/timer.");
							throw new RuntimeException("TODO: Add to node out buf/timer."); 
						}
					}
					else { 
						logger.error("TODO: Reliablity handling.");
						System.exit(1); 
					}
					*/
				}
				else
				{
					logger.debug("Failed to send tuple "+dt.getPayload().timestamp +" to "+targets.get(0));
				}
				
			}
		}
	}
	
	
	public class DispatcherWorker implements Runnable
	{
		//private final BlockingQueue<DataTuple> tupleQueue = new LinkedBlockingQueue<DataTuple>();	//TODO: Want a priority set perhaps?
		//private final BlockingQueue<DataTuple> tupleQueue = new ArrayBlockingQueue<DataTuple>(1);
		private final Exchanger<DataTuple> exchanger = new Exchanger<>();
		private final OutputQueue outputQueue;
		private final EndPoint dest;
		
		public DispatcherWorker(OutputQueue outputQueue, EndPoint dest)
		{
			this.outputQueue = outputQueue;
			this.dest = dest;
		}
		
		/*
		public void send(DataTuple dt)
		{
			//TODO: this needs to be thread safe.
			if (!tupleQueue.contains(dt))
			{
				tupleQueue.add(dt);
			}
			else
			{
				logger.info("Discarding duplicate retransmit "+dt.getPayload().timestamp);
			}
		}
		*/
		
		public boolean trySend(DataTuple dt, long timeout)
		{
			//TODO: this needs to be thread safe.
			/*
			if (!tupleQueue.contains(dt))
			{
				try {
					return tupleQueue.offer(dt, timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					return false;
				}
			}
			else
			{
				logger.info("Discarding duplicate retransmit "+dt.getPayload().timestamp);
				return true;
			}
			*/
			try {
				exchanger.exchange(dt, timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | TimeoutException e) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run()
		{
			while (true)
			{				
				DataTuple nextTuple = null;
				try {
					//Use an exchanger instead of a tupleQueue.
					nextTuple = exchanger.exchange(null);
					//nextTuple = tupleQueue.take();					
				} catch (InterruptedException e) {
					throw new RuntimeException("TODO: Addition and removal of downstreams.");
				}
				logger.debug("Dispatcher sending tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);

				//nextTuple.getPayload().instrumentation_ts=System.currentTimeMillis();
				boolean success = outputQueue.sendToDownstream(nextTuple, dest);
				if (success)
				{
					logger.debug("Dispatcher sent tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
				}
				else
				{
					//Connection must be down.
					//Remove any output tuples from this replica's output log and add them to the operator output queue.
					//This should include the current 'SEEP' batch since it might contain several tuples.
					List<OutputLogEntry> logged = ((SynchronousCommunicationChannel)dest).getBuffer().trim(null);
					
					if(!optimizeReplay)
					{
						requeueTuples(logged);
					}
					else
					{
						//holding the lock
						//1) Save the alives from the old fctrl for this downstream
						//2) delete the old fctrl for this downstream (TODO: What if control connection still open?)
						//3) compute the new joint alives
						//4) Do a combined.set alives
						//5) for tuple in logged
						//		if acked discard
						//		else if in joint alives add to shared replay log
						//		else add to output queue 
						//
						//		remove from the alives for the old fctrl for this downstream
						//6) For remaining tuples in old fctrl for this downstream
						//		if tuple not acked and not in new joint alives and tuple in shared replay log
						//			move tuple from shared replay log to output queue
						
					}
					
					//Update this connections routing cost 
					//TODO: Double check the downstream operator id you're using here is the same as the routing control worker
					owner.getOperator().getRouter().update_highestWeight(new DownUpRCtrl(dest.getOperatorId(), -1.0, null));
					//TODO: Remove the alives for this downstream?
					//TODO: Interrupt the dispatcher thread
					
					//Reconnect synchronously (might need to add a helper method to the output queue).
					outputQueue.reopenEndpoint(dest);
				}
			}
		}
		
		private void requeueTuples(List<OutputLogEntry> logged)
		{
			for (OutputLogEntry o: logged)
			{
				for (TuplePayload p : o.batch.batch)
				{
					DataTuple dt = new DataTuple(idxMapper, p);
					logger.debug("Requeueing data tuple with timestamp="+p.timestamp);
					opQueue.add(dt);
				}
			}
		}
		
		public boolean purgeSenderQueue()
		{	
			/*
			logger.error("TODO: Thread safety?"); 
			boolean changed = false;
			FailureCtrl currentFctrl = getNodeFailureCtrl();
			Iterator<DataTuple> qIter = tupleQueue.iterator();
			while (qIter.hasNext())
			{
				long batchId = qIter.next().getPayload().timestamp;
				if (batchId <= currentFctrl.lw() || currentFctrl.acks().contains(batchId) || currentFctrl.alives().contains(batchId))
				{
					changed = true;
					qIter.remove();
				}
			}
			*/
			//return changed;

			//TODO: Don't think I really need to bother given
			//the tuple queue is only of size 1? Could perhaps
			//optimize later.
			throw new RuntimeException("TODO");
		}
		
		/*
		public int queueLength()
		{
			//TODO: Thread safe?
			return tupleQueue.size();
		}
		*/
	
	}
	
	public class RoutingControlWorker implements Runnable
	{
		private final int downId;
		
		public RoutingControlWorker(int downId) {
			this.downId = downId;
		}

		@Override
		public void run() {
			// while true
			while (true)
			{
				/*
				int tupleQueueLength = 0;
				int bufLength = 0;
				int downIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downId);
				synchronized(lock)
				{
					//measure tuple queue length
					tupleQueueLength = workers.get(downIndex).queueLength();
					//measure buf length
					bufLength = bufLength(downId);
				}	
				int totalQueueLength = tupleQueueLength + bufLength;
				logger.debug("Total queue length to "+downId + " = "+ totalQueueLength+"("+tupleQueueLength+"/"+bufLength+")");
				//Create and send control tuple
				sendQueueLength(totalQueueLength);
				*/
				int totalQueueLength = getTotalQlen();
				//N.B. TODO: This doesn't include anything in the arrayblockingqueues.
				logger.debug("Total queue length to "+downId + " = "+ totalQueueLength);
				//Create and send control tuple
				sendQueueLength(totalQueueLength);
				
				//wait for interval
				try {
					Thread.sleep(ROUTING_CTRL_DELAY);
				} catch (InterruptedException e) {}
			}
		}
		
		/*
		private int bufLength(int opId)
		{
			SynchronousCommunicationChannel cci = owner.getPUContext().getCCIfromOpId(opId, "d");
			if (cci != null)
			{
				IBuffer buffer = cci.getBuffer();
				return buffer.numTuples();
			}
			else { throw new RuntimeException("Logic error."); }
		}
		*/
		
		private void sendQueueLength(int queueLength)
		{
			int localOpId = owner.getOperator().getOperatorId();
			ControlTuple ct = new ControlTuple(ControlTupleType.UP_DOWN_RCTRL, localOpId, queueLength);
			logger.debug("Sending control tuple downstream from "+localOpId+" with queue length="+queueLength);
			boolean flushSuccess = owner.getOwner().getControlDispatcher().sendDownstream(ct, owner.getOperator().getOpContext().getDownOpIndexFromOpId(downId), false);
			if (!flushSuccess)
			{
				owner.getOperator().getRouter().update_highestWeight(new DownUpRCtrl(downId, -1.0, null));
			}
		}
	}
	
	private class FailureCtrlHandler
	{

		public void handleFailureCtrl(FailureCtrl fctrl, int dsOpId) 
		{

			
			// Holding lock:

			//1) Remember combined.lw and combined.acks.size
			//2) combined.update(fctrl.lw, fctrl.acks, null)
			//3) Compute the new joint alives
			//4) combined.setAlives(joint alives)
			//5) For alive in ds.oldalives
			//		//TODO: Could this be racy if already replaying (e.g. data vs ctrl conn independent failures)
			//		if alive > combined.lw and not in combined.acks and not in fctrl.alives
			//           move from shared replay to output queue
			//6) Set ds.oldalives = fctrl.alives   (N.B. Some of these might actually have been acked.
			// Maybe release lock and take a copy of combined? Maybe not though, want to protect replay log?
			//7) if combined.lw > old combined.lw or combined.acks.size > combined.old.acks.size
			//		trim output bufs & shared replay log
			//		if aggressive purge
			//			trim output queue
			
			synchronized(lock)
			{
				logger.debug("Handling failure ctrl received from "+dsOpId+",cdfctrl="+combinedDownFctrl+ ", fctrl="+fctrl);
				long oldLw = combinedDownFctrl.lw();
				long oldAcksSize = combinedDownFctrl.acks().size();
				combinedDownFctrl.update(fctrl.lw(), fctrl.acks(), null);
				boolean acksChanged = oldLw < combinedDownFctrl.lw() || oldAcksSize < combinedDownFctrl.acks().size();
				
				if (optimizeReplay)
				{
					Set<Long> newAlives = new HashSet<>();
					newAlives.addAll(fctrl.alives());
					for (Integer id : downAlives.keySet())
					{
						if (id != dsOpId && downAlives.get(id) != null) 
						{ 
							newAlives.addAll(downAlives.get(id)); 
						}
					}

					combinedDownFctrl.setAlives(newAlives);
					if (downAlives.get(dsOpId) != null)
					{
						for (Long oldDownAlive : downAlives.get(dsOpId))
						{
							if (oldDownAlive > combinedDownFctrl.lw() && 
									!combinedDownFctrl.acks().contains(oldDownAlive) &&
									!newAlives.contains(oldDownAlive))
							{
								//Retraction, should schedule for replay if in shared replay log.
								DataTuple dt = sharedReplayLog.remove(oldDownAlive);
								if (dt != null) { opQueue.add(dt); }
							}			
						}
					}
					downAlives.put(dsOpId, fctrl.alives());
				}
				
				if (acksChanged)
				{
					purgeSharedReplayLog();
					//TODO: Think it's ok to temporarily miss tuples being batched but not currently in log?
					purgeSenderBuffers();
					
					if (aggressivePurge) { purgeOpOutputQueue(); }
				}
				
				synchronized(lock) { lock.notifyAll(); }
			}
		}
		
		private boolean purgeSenderBuffers()
		{
			//TODO: How to trim buffer?
			FailureCtrl currentFailureCtrl = getCombinedDownFailureCtrl();
			for (int opId : workers.keySet())
			{
				SynchronousCommunicationChannel cci = owner.getPUContext().getCCIfromOpId(opId, "d");
				if (cci != null)
				{
					IBuffer buffer = cci.getBuffer();
					buffer.trim(currentFailureCtrl);
				}
			}
			return true;
		}
		
		private boolean purgeOpOutputQueue()
		{
			boolean changed = false;
			changed = !opQueue.removeOlderInclusive(combinedDownFctrl.lw()).isEmpty();
			changed = changed || !opQueue.removeAll(combinedDownFctrl.acks()).isEmpty();
			//TODO: Move alives to shared replay buffer?
			return changed;			
		}
	}
	
	public class OperatorOutputQueue
	{
		private SortedMap<Long, DataTuple> queue;
		private final int maxSize;
		
		public OperatorOutputQueue(int maxSize)
		{
			this.maxSize = maxSize;
			queue = new TreeMap<>();
		}
		
		public void add(DataTuple dt)
		{
			synchronized(lock)
			{
				while (queue.size() > maxSize)
				{
					try { lock.wait();} 
					catch (InterruptedException e) {}
				}
				queue.put(dt.getPayload().timestamp, dt);
				lock.notifyAll();
			}
		}
		
		public int size() { synchronized(lock) { return queue.size(); }}
		public DataTuple remove(long ts)
		{
			DataTuple dt = null;
			synchronized(lock)
			{
				dt = queue.remove(ts);
				if (dt != null) { lock.notifyAll() ; }
			}
			return dt;
		}
		
		public Map<Long, DataTuple> removeAll(Set<Long> tsSet)
		{
			Map<Long, DataTuple> removed = new TreeMap<>();
			synchronized(lock)
			{
				//Optimization - iterate over the smaller of the q and the tsSet
				if (tsSet.size() < queue.size())
				{
					for (Long ts : tsSet)
					{
						DataTuple dt = queue.remove(ts);
						if (dt != null) { removed.put(ts,  dt); }
					}
				}
				else
				{
					for (Long qts : queue.keySet())
					{
						if (tsSet.contains(qts))
						{
							DataTuple dt = queue.remove(qts);
							if (dt != null) { removed.put(qts,  dt); }
						}
					}
				}
				if (!removed.isEmpty()) { lock.notifyAll(); }
			}
			return removed;
		}
		
		public SortedMap<Long, DataTuple> removeOlderInclusive(long ts)
		{
			synchronized(lock)
			{
				SortedMap<Long, DataTuple> removed = new TreeMap<>(queue.headMap(ts+1));
				SortedMap<Long, DataTuple> remainder = queue.tailMap(ts+1);
				if (remainder == null || remainder.isEmpty()) { queue.clear(); }
				else { queue = remainder; }
				if (removed != null && !removed.isEmpty()) { lock.notifyAll(); }
				return removed;
			}
		}
		
		public boolean contains(long ts)
		{
			synchronized(lock) { return queue.containsKey(ts); }
		}
		
		public boolean isEmpty()
		{
			synchronized(lock) { return queue.isEmpty(); }
		}
		
		public DataTuple tryRemoveHead()
		{
			synchronized(lock)
			{
				if (queue.isEmpty()) { return null; }
				else 
				{ 
					DataTuple dt = queue.remove(queue.firstKey());
					lock.notifyAll();
					return dt;
				}
			}
		}
		
		public DataTuple tryPeekHead()
		{
			synchronized(lock)
			{
				if (queue.isEmpty()) { return null; }
				else { return queue.get(queue.firstKey()); }
			}
		}
		
	}
}
