package uk.ac.imperial.lsds.seep.processingunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.comm.routing.IRoutingObserver;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

public class Dispatcher implements IRoutingObserver {

	//private final Map<Integer, DataTuple> senderQueues = new HashMap<Integer, ConcurrentNavigableMap<Integer, DataTuple>>();
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private static final long FAILURE_TIMEOUT = 60 * 1000;
	private static final long RETRANSMIT_CHECK_INTERVAL = 10 * 1000;
	private static final long ROUTING_CTRL_DELAY = 1 * 1000;
	private static final long SEND_TIMEOUT = 1 * 1000;
	private int MAX_NODE_OUT_BUFFER_TUPLES = Integer.MAX_VALUE;
	private final int MAX_TOTAL_QUEUE_SIZE;
	private final FailureCtrl nodeFctrl = new FailureCtrl();
	private final Map<Long, DataTuple> nodeOutBuffer = new LinkedHashMap<>();
	private final Map<Long, Long> nodeOutTimers = new LinkedHashMap<>();	//TODO: Perhaps change to a delayQueue
	private final Map<Integer, DispatcherWorker> workers = new HashMap<>();
	private final Set<RoutingControlWorker> rctrlWorkers = new HashSet<>();
	private final FailureDetector failureDetector = new FailureDetector();
	private final FailureCtrlHandler fctrlHandler = new FailureCtrlHandler();
	private final IProcessingUnit owner;
	private ArrayList<OutputQueue> outputQueues;
	private final boolean bestEffort;
	private final OperatorOutputQueue opQueue;

	
	private final Object lock = new Object(){};
	
	public Dispatcher(IProcessingUnit owner)
	{
		this.owner = owner;
		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		MAX_TOTAL_QUEUE_SIZE = Integer.parseInt(GLOBALS.valueFor("maxTotalQueueSizeTuples"));
		//opQueue = new OperatorOutputQueue(Integer.MAX_VALUE);
		opQueue = new OperatorOutputQueue(MAX_TOTAL_QUEUE_SIZE);
	}
	
	public FailureCtrl getNodeFailureCtrl()
	{
		FailureCtrl copy = null;
		synchronized(lock)
		{
			copy = new FailureCtrl(nodeFctrl);
		}
		return copy;
	}
	public void setOutputQueues(ArrayList<OutputQueue> outputQueues)
	{
		//TODO: Not sure how well this will work wrt connection updates,
		//threading etc.
		this.outputQueues = outputQueues;
		
		for(int i = 0; i < outputQueues.size(); i++)
		{
			//1 thread per worker - assumes fan-out not too crazy and that we're network bound.
			DispatcherWorker worker = new DispatcherWorker(outputQueues.get(i), owner.getPUContext().getDownstreamTypeConnection().elementAt(i));			
			Thread workerT = new Thread(worker);
			workers.put(i, worker);
			workerT.start();
		}
		
		
	}
	
	public void startRoutingCtrlWorkers()
	{
		
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
		for(Integer downOpId : owner.getOperator().getOpContext().getDownstreamOpIdList())
		{
			//1 thread per worker - assumes fan-out not too crazy and that we're network bound.
			RoutingControlWorker worker = new RoutingControlWorker(downOpId);			
			Thread workerT = new Thread(worker);
			rctrlWorkers.add(worker);
			workerT.start();
		}	
		
		//TODO: Is this safe?
		Thread mainT = new Thread(new DispatcherMain());
		mainT.start();
	}
	
	public void startFailureDetector()
	{
		if (bestEffort)
		{
			logger.error("Logic error - starting failure detector for best effort reliability.");
			System.exit(1);
		}
		Thread fDetectorT = new Thread(failureDetector);
		fDetectorT.start();
	}
	
	/**
	 * TODO: Need to rearrange locking so that caller can block on 
	 * a full node out buffer without causing a deadlock.
	 */
	public void dispatch(DataTuple dt) { dispatch(dt, false); }
	public void dispatch(DataTuple dt, boolean retransmission)
	{
		if (!bestEffort)
		{
			dispatchReliable(dt, retransmission); 
		}
		else
		{
			dispatchBestEffort(dt);
		}	
	}
	
	private void dispatchBestEffort(DataTuple dt)
	{
		//TODO: for reliable should check if already in queue or node out buf.
		opQueue.add(dt);
	}
	
	private void dispatchReliable(DataTuple dt, boolean retransmission) { throw new RuntimeException("TODO"); }
	
	
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
	
	
	public void ack(DataTuple dt)
	{
		if (!bestEffort)
		{
			throw new RuntimeException("TODO");
			/*
			 TODO: Should be acking selectively here?
			 Or perhaps refactor.
			long ts = dt.getPayload().timestamp;
			synchronized(lock)
			{
				nodeFctrl.ack(ts);
			}
			*/		
		}
		

	}
	
	public FailureCtrl handleFailureCtrl(FailureCtrl fctrl, int dsOpId) 
	{
		fctrlHandler.handleFailureCtrl(fctrl, dsOpId);
		FailureCtrl toUpstream = new FailureCtrl(nodeFctrl);
		synchronized(lock)
		{
			toUpstream.updateAlives(nodeOutBuffer.keySet());
		}
		//return toUpstream;
		throw new RuntimeException("TODO"); 
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
				if (dt != null) { targets = owner.getOperator().getRouter().forward_highestWeight(dt); }
				while(dt == null || targets == null || targets.isEmpty())
				{
					synchronized(lock)
					{
						try { lock.wait(); } catch (InterruptedException e) {}
					}
					dt = opQueue.tryPeekHead();
					if (dt != null) { targets = owner.getOperator().getRouter().forward_highestWeight(dt); }
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
					if (dt != null)
					{
						if (!bestEffort) { throw new RuntimeException("TODO: Add to node out buf/timer."); }
					}
					else { throw new RuntimeException("TODO: Reliability handling."); }
				}
			}
		}
	}
	
	
	public class DispatcherWorker implements Runnable
	{
		//private final BlockingQueue<DataTuple> tupleQueue = new LinkedBlockingQueue<DataTuple>();	//TODO: Want a priority set perhaps?
		private final BlockingQueue<DataTuple> tupleQueue = new ArrayBlockingQueue<DataTuple>(1);
		private final OutputQueue outputQueue;
		private final EndPoint dest;
		
		public DispatcherWorker(OutputQueue outputQueue, EndPoint dest)
		{
			this.outputQueue = outputQueue;
			this.dest = dest;
		}
		
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
		
		public boolean trySend(DataTuple dt, long timeout)
		{
			//TODO: this needs to be thread safe.
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
		}
		
		@Override
		public void run()
		{
			while (true)
			{				
				DataTuple nextTuple = null;
				try {
					nextTuple = tupleQueue.take();					
				} catch (InterruptedException e) {
					throw new RuntimeException("TODO: Addition and removal of downstreams.");
				}
				logger.debug("Dispatcher sending tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
				outputQueue.sendToDownstream(nextTuple, dest);
				logger.debug("Dispatcher sent tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
			}
		}
		
		public boolean purgeSenderQueue()
		{
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
			return changed;
		}
		
		public int queueLength()
		{
			//TODO: Thread safe?
			return tupleQueue.size();
		}
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
	
	/** Resends timed-out tuples/batches, possibly to a different downstream. */
	public class FailureDetector implements Runnable
	{	
		public void run()
		{
			//if (outputQueues.size() <= 1) { throw new RuntimeException("No need?"); }
			
			while(true)
			{
				checkForRetransmissions();
				
				//TODO: This will enforce a minimum wait of RETRANSMIT_CH
				synchronized(lock)
				{
					long waitStart = System.currentTimeMillis();
					long now = waitStart;
					
					while (waitStart + RETRANSMIT_CHECK_INTERVAL > now)
					{
						try {
							lock.wait((waitStart + RETRANSMIT_CHECK_INTERVAL) - now);
						} catch (InterruptedException e) {}
						now = System.currentTimeMillis();
					}
				}
			}
		}
		
		public void checkForRetransmissions()
		{
			Set<Long> timerKeys = new HashSet<Long>();
			synchronized(lock)
			{
				int maxRetransmits = MAX_TOTAL_QUEUE_SIZE - opQueue.size();
				if (maxRetransmits <= 0) 
				{
					logger.info("Skipping retransmission as no space in output queues.");
					return;
				}
				
				//TODO: Really don't want to be holding the lock for this long.
				for(Map.Entry<Long, Long> entry : nodeOutTimers.entrySet())
				{
					if (entry.getValue() + FAILURE_TIMEOUT  < System.currentTimeMillis())
					{
						timerKeys.add(entry.getKey());
						if (timerKeys.size() > maxRetransmits)
						{
							//TODO: This is a bit arbitrary, should probably sort by age and then trim?
							break;
						}
						//entry.setValue(System.currentTimeMillis());
					}
				}
			}
			
			//TODO: This is a best effort retransmit. If there is no
			//available downstream for a particular tuple, it will have
			//to wait until the next failure detection timeout. Should
			//possibly tweak to retry in the meantime whenever there is
			//a routing change.
			for (Long tupleKey : timerKeys)
			{
				DataTuple dt = null;
				
				synchronized(lock)
				{
					dt = nodeOutBuffer.get(tupleKey);
				}
								
				if (dt != null)	
				{
					int target = -1;
					ArrayList<Integer> targets = owner.getOperator().getRouter().forward_highestWeight(dt);
					if (targets.size() > 1) { throw new RuntimeException("TODO"); }
					else if (targets.size() == 1) { target = targets.get(0); }

					if (target >= 0)
					{
						logger.warn("Retransmitting tuple "+dt.getPayload().timestamp +" to "+target);
						workers.get(target).send(dt);
						synchronized(lock)
						{
							//If hasn't been acked
							if (nodeOutTimers.containsKey(dt.getPayload().timestamp))
							{
								//Touch the retransmission timer
								nodeOutTimers.put(dt.getPayload().timestamp, System.currentTimeMillis());
							}
						}
					}
					else
					{
						//TODO: This might change once there are 'unmatched' hints.
						return;
					}
				}
			}					
		}
	}
	
	
	private class FailureCtrlHandler
	{

		public void handleFailureCtrl(FailureCtrl fctrl, int dsOpId) 
		{
			logger.debug("Handling failure ctrl received from "+dsOpId+",nodefctrl="+nodeFctrl+ ", fctrl="+fctrl);
			nodeFctrl.update(fctrl);
			boolean nodeOutChanged = purgeNodeOut();
			refreshNodeOutTimers(fctrl.alives());
			boolean senderOutsChanged = purgeSenderQueues();
			boolean senderBuffersChanged = purgeSenderBuffers();

			synchronized(lock) { lock.notifyAll(); }
		}
		private boolean purgeNodeOut()
		{
			boolean changed = false;
			synchronized(lock)
			{
				logger.debug("Handling failure ctrl = "+nodeFctrl+", with node out buf size="+nodeOutBuffer.size());
				Iterator<Long> iter = nodeOutBuffer.keySet().iterator();
				while (iter.hasNext())
				{
					long nxtBatch = iter.next();
					if (nxtBatch <= nodeFctrl.lw() || nodeFctrl.acks().contains(nxtBatch))
					{
						iter.remove();
						nodeOutTimers.remove(nxtBatch);
						changed = true;
					}
				}
				if (changed) { lock.notifyAll(); }
				logger.debug("Post purge node out buf size="+nodeOutBuffer.size());
			}
			return changed;
		}

		private void refreshNodeOutTimers(Set newAlives)
		{
			synchronized(lock)
			{
				Iterator<Long> iter = nodeFctrl.alives().iterator();
				while (iter.hasNext())
				{
					Long nxtAlive = (Long)iter.next();
					if (newAlives.contains(nxtAlive))
					{
						//Only refresh the newly updated alives
						//TODO: Could perhaps use the time the fctrl was sent instead.
						//Note that newAlives might contain the ids of tuples never
						//received at this operator. In theory we could keep track of them
						//in order to forward them upstream even though we can't replay
						//them directly from this node in the event of a timeout.
						nodeOutTimers.put(nxtAlive, System.currentTimeMillis());
					}
				}
			}
		}

		private boolean purgeSenderQueues()
		{
			boolean changed = false;
			for (DispatcherWorker worker : workers.values())
			{
				if (worker.purgeSenderQueue()) { changed = true; }
			}
			return changed;
		}

		private boolean purgeSenderBuffers()
		{
			//TODO: How to trim buffer?
			FailureCtrl currentFailureCtrl = getNodeFailureCtrl();
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
