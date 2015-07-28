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
import java.util.Timer;
import java.util.TimerTask;
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
import uk.ac.imperial.lsds.seep.buffer.OutOfOrderBuffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.routing.IRoutingObserver;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.manet.Query;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import static uk.ac.imperial.lsds.seep.manet.MeanderMetricsNotifier.notifyThat;

public class Dispatcher implements IRoutingObserver {

	//private final Map<Integer, DataTuple> senderQueues = new HashMap<Integer, ConcurrentNavigableMap<Integer, DataTuple>>();
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private static final long ROUTING_CTRL_DELAY = 1 * 500;
	private static final long SEND_TIMEOUT = 1 * 500;
	private final int MAX_TOTAL_QUEUE_SIZE;
	private final boolean bestEffort;
	private final boolean optimizeReplay;
	private final boolean eagerPurgeOpQueue;
	private final boolean downIsMultiInput;
	private final boolean boundedOpQueue;
	
	private final IProcessingUnit owner;
	private final Map<Integer, DispatcherWorker> workers = new HashMap<>();
	private final Set<RoutingControlWorker> rctrlWorkers = new HashSet<>();
	private final FailureCtrlHandler fctrlHandler = new FailureCtrlHandler();
	private final FailureCtrlWatchdog failureCtrlWatchdog;
	
	private ArrayList<OutputQueue> outputQueues;
	private final OperatorOutputQueue opQueue;
	private final OperatorOutputQueue sharedReplayLog;
	//private final FailureCtrl nodeFctrl = new FailureCtrl();	//KEEP THIS
	private final FailureCtrl combinedDownFctrl = new FailureCtrl();
	private final Map<Integer, Set<Long>> downAlives = new HashMap<>();	//TODO: Concurrency?
	
	private final Map<String, Integer> idxMapper = new HashMap<String, Integer>(); //Needed for replay after conn failure
	
	private final Object lock = new Object(){};
	
	public Dispatcher(IProcessingUnit owner)
	{
		this.owner = owner;
		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		
		if (bestEffort)
		{
			Query meanderQuery = owner.getOperator().getOpContext().getMeanderQuery(); 
			int logicalId = meanderQuery.getLogicalNodeId(owner.getOperator().getOperatorId());
			int downLogicalId = meanderQuery.getNextHopLogicalNodeId(logicalId); 
			if (meanderQuery.getLogicalInputs(downLogicalId).length > 1)
			{
				throw new RuntimeException("TODO");
			}
		}
		
		downIsMultiInput = true;
		optimizeReplay = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay"));
		eagerPurgeOpQueue = Boolean.parseBoolean(GLOBALS.valueFor("eagerPurgeOpQueue"));
		boundedOpQueue = !GLOBALS.valueFor("meanderRouting").equals("backpressure") || Boolean.parseBoolean(GLOBALS.valueFor("boundMeanderRoutingQueues"));
		
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
		
		//Not really an output queue but can reuse code. Possible unnecessary lock contention?
		sharedReplayLog = new OperatorOutputQueue(Integer.MAX_VALUE);	

		for(int i = 0; i<owner.getOperator().getOpContext().getDeclaredWorkingAttributes().size(); i++){
			idxMapper.put(owner.getOperator().getOpContext().getDeclaredWorkingAttributes().get(i), i);
		}
		
		if (Boolean.parseBoolean(GLOBALS.valueFor("enableFailureCtrlWatchdog")) && 
				!owner.getOperator().getOpContext().isSink())
		{
			failureCtrlWatchdog = new FailureCtrlWatchdog();
		}
		else { failureCtrlWatchdog = null; }		
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
			EndPoint dest = owner.getPUContext().getDownstreamTypeConnection().elementAt(i);
			DispatcherWorker worker = new DispatcherWorker(outputQueues.get(i), dest);			
			Thread workerT = new Thread(worker, "DispatcherWorker-"+dest.getOperatorId());
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
			Thread workerT = new Thread(worker, "RoutingControlWorker-"+downOpId);
			rctrlWorkers.add(worker);
			workerT.start();
		}
		
		logger.info("Started dispatcher routing control workers.");
	}
	
	public void startDispatcherMain()
	{
		//TODO: Is this safe?
		Thread mainT = new Thread(new DispatcherMain(), "DispatcherMain");
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
		synchronized(lock)
		{
			if (combinedDownFctrl.lw() >= ts || combinedDownFctrl.acks().contains(ts))
			{
				//Acked already, discard.
				return;	
			}
			if (!(optimizeReplay && combinedDownFctrl.alives().contains(ts)))
			{
				if (owner.getOperator().getOpContext().isSource() || boundedOpQueue)
				{
					//Schedule for sending
					opQueue.add(dt);
				}
				else
				{
					//TODO: Only if bp routing surely?
					opQueue.forceAdd(dt);
				}
			}
			else
			{
				//Live downstream already, save it in shared replay log
				//Shouldn't be in per-sender log since would have been
				//detected as a dupe at input.
				logger.info("Dispatcher avoided sending live tuple: "+ts);
				sharedReplayLog.add(dt);
			}
		}
	}
	
	public int getTotalQlen()
	{
		return opQueue.size();
	}
	
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
		return getCombinedDownFailureCtrl();
	}
	
	public FailureCtrl handleUpstreamFailureCtrl(FailureCtrl fctrl, int upOpId)
	{
		if (bestEffort || !owner.getOperator().getOpContext().isSink()) { 
			throw new RuntimeException("Logic error - best effort or not a sink."); 
		}
		fctrlHandler.handleUpstreamFailureCtrl(fctrl, upOpId);
		FailureCtrl result = getCombinedDownFailureCtrl();
		logger.debug("Handled upstream failure ctrl: upFctrl="+fctrl+",updated="+result);
		return result;
	}
	
	/*
	public void routingChanged()
	{
		synchronized(lock) { lock.notifyAll(); }
	}
	*/
	
	//N.B. newConstraints will contain all constrains for the given input,
	//but only if there was a change from the previous constraints.
	public void routingChanged(Map<Integer, Set<Long>> newConstraints)
	{
        //for each dj in newConstraints.keySet
        //	for each batch id i in rctrl(dj).constraints():
        //        if not i in opQueue or sessionsLogs(j)
        //                if !optimize replay or optimizeReplay and i not in alives dj
        //                        if i in shared replay log
        //                                mv to opQueue
        //                        else if i in other session log
        //                                copy to output queue

		logger.debug("Routing changed, new constraints="+newConstraints);
		long tStart = System.currentTimeMillis();
		if (newConstraints != null)
		{
			if (newConstraints.size() > 1) { throw new RuntimeException("Logic error."); }
			for (Integer downOpId : newConstraints.keySet())
			{
				for (Long ts : newConstraints.get(downOpId))
				{
					int target = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
					synchronized(lock)
					{
						if (!opQueue.contains(ts) && !workers.get(target).inSessionLog(ts))
						{
							logger.trace("Constrained tuple "+ts+" not in op queue or session log "+target+"/"+downOpId);
							if (!optimizeReplay || 
									(optimizeReplay && !isDownAlive(downOpId, ts)))
							{
								logger.trace("Constrained tuple "+ts+ "+not in alives.");
								DataTuple dt = sharedReplayLog.remove(ts);
								if (dt == null)
								{
									logger.trace("Constrained tuple not in shared replay log.");
									for (Integer workerIndex : workers.keySet())
									{
										if (target != workerIndex)
										{
											dt = workers.get(workerIndex).getFromSessionLog(ts);
										}
										if (dt != null) { break; }
										else { logger.trace("Constrained tuple "+ts+" not in session log "+workerIndex); }
									}
								}
								if (dt != null)
								{
									logger.info("Readding constrained tuple "+ts+" to op queue.");
									opQueue.forceAdd(dt);
								}
							}
						}
						lock.notifyAll();
					}
				}
			}
			
			synchronized(lock) { lock.notifyAll(); }
			logger.debug("Finished handling routing change, duration=" + ((System.currentTimeMillis() - tStart)/1000));
		}
	}
	
	private boolean isDownAlive(int downOpId, long ts)
	{
		synchronized(lock) { return downAlives.get(downOpId) != null && downAlives.get(downOpId).contains(ts); }
	}
	
	public void stop(int target) { throw new RuntimeException("TODO"); }
	
	
	public class DispatcherMain implements Runnable
	{
		public DispatcherMain() {}
		public void run()
		{
			while(true)
			{
				Set<Long> constraints = dispatchHead();
				
				if (constraints != null && !constraints.isEmpty())
				{
					dispatchConstrained(constraints);
				}
			}
		}

		private Set<Long> dispatchHead()
		{
			DataTuple dt = null;
			ArrayList<Integer> targets = null; 
			Set<Long> constraints = null;
			
			synchronized(lock)
			{
				dt = peekNext();
				targets = owner.getOperator().getRouter().forward_highestWeight(dt);
				logger.debug("Dispatcher peeked at head tuple "+dt.getPayload().timestamp + " with targets "+targets);

				if (targets == null || targets.isEmpty())
				{
					//TODO: Really don't like doing this while holding the lock.
					constraints = owner.getOperator().getRouter().areConstrained(opQueue.keys());

					if (constraints == null || constraints.isEmpty())
					{
						//Check unconstrained tuple isn't alive downstream already.
						if (admitBatch(dt, null))
						{
							logger.debug("Dispatcher waiting for routing change or constrained tuples.");
							//If no constraints, wait for some queue/routing change and then loop back
							try { lock.wait(); } catch (InterruptedException e) {}
						}
						return null;
					}
					//else fall through and try to route from further back in the queue
					logger.debug("Dispatcher found constrained tuples in queue: "+constraints);
				}
			}
			
			//if head of queue has a target (i.e. positive differential with some ds)
			if (targets != null && !targets.isEmpty())
			{
				//assert constraints null or constraints empty
				//N.B. The head tuple could still be constrained, we just shouldn't have
				//explicitly retrieved the set of all currently constrained tuples!
				logger.debug("Sending head tuple "+dt.getPayload().timestamp +" to "+targets);
				
				//TODO: Should perhaps do admission before dispatch? Don't want
				//to be waiting unnecessarily on a batch that should be trimmed?
				if (admitBatch(dt, targets))
				{
					sendBatch(dt, targets);
				}
				return null;
			}
			else
			{
				if (constraints == null || constraints.isEmpty()) { throw new RuntimeException("Logic error.");}
				logger.debug("Cannot dispatch from head, will try with constraints: "+constraints);
				return constraints;
			}
			
			//return constraints;
		}
		
		private void dispatchConstrained(Set<Long> constraints)
		{
			//Otherwise try to route constrained from further back in the queue
			if (constraints == null || constraints.isEmpty()) { throw new RuntimeException("Logic error."); }
			
			int constrainedInQueue = 0;
			//for each batch in constraints
			for (Long ts : constraints)
			{
				//if opQueue has batch
				DataTuple dt = opQueue.get(ts);
				if (dt != null)
				{
					constrainedInQueue++;
					ArrayList<Integer> targets = owner.getOperator().getRouter().forward_highestWeight(dt);
					logger.debug("Dispatching non-head constrained tuple "+ts+" to targets "+targets);
					if (targets != null && !targets.isEmpty())
					{
						// if admit batch
						if (admitBatch(dt, targets))
						{
							//sendBatch
							//TODO: Should somehow enforce that this send must be constrained?
							sendBatch(dt, targets);
							//TODO: Should we go back to the head of the queue if any succeeded?
						}
					}
				}
			}
			logger.debug("Found "+constrainedInQueue+" of "+constraints.size() + " tuples in queue");
			if (constrainedInQueue == 0)
			{
				logger.debug("Constraints="+constraints+", keys="+opQueue.keys());
			}
		}
		
		//Should be holding lock.
		private DataTuple peekNext()
		{
			DataTuple dt = null;
			while (dt == null)
			{
				dt = opQueue.peekHead();
				long ts = dt.getPayload().timestamp;
				if (ts <= combinedDownFctrl.lw() || combinedDownFctrl.acks().contains(ts))
				{
					opQueue.remove(ts);
					dt = null;
				}
			}
			return dt;
		}
		
		//Check whether we should send this batch, discard it, or move
		//it to the shared replay log.
		private boolean admitBatch(DataTuple dt, ArrayList<Integer> targets)
		{
			FailureCtrl fctrl = getCombinedDownFailureCtrl();
			long ts = dt.getPayload().timestamp;
			if (!shouldTrySend(ts, fctrl, isConstrainedRoute(targets)))
			{
				dt = opQueue.remove(dt.getPayload().timestamp);
				
				//Alive downstream, so don't discard in case we need
				//to replay if the downstream fails.
				if (fctrl.alives().contains(ts) && dt != null)
				{
					logger.info("Replay optimization: dispatcher avoided replaying tuple "+ts);
					sharedReplayLog.add(dt);
				}
				
				return false;
			}
			
			return true;
		}
		
		private void sendBatch(DataTuple dt, ArrayList<Integer> targets)
		{
			if (targets == null) { throw new RuntimeException("Logic error."); }
			boolean constrainedRoute = isConstrainedRoute(targets);
			long ts = dt.getPayload().timestamp;
			
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
			//could hurt parallelism? Can perhaps avoid with priorities. ;
			if (!constrainedRoute)
			{
				logger.debug("Sending unconstrained tuple "+ ts);
				//Could already be in session log if sending to multi-input op.
				int target = targets.get(0);
				boolean remove = false;
				if (downIsMultiInput && workers.get(target).inSessionLog(ts))
				{
					logger.info("Skipping unconstrained transmission of "+ts+", already in session log");
					remove = true;
				}
				else
				{
					remove = workers.get(target).trySend(dt, SEND_TIMEOUT);
					notifyThat(owner.getOperator().getOperatorId()).triedSend();
					if (remove) { notifyThat(owner.getOperator().getOperatorId()).sendSucceeded(); }
					else
					{
						ArrayList<Integer> nextTargets = owner.getOperator().getRouter().forward_highestWeight(dt);
						if (nextTargets != null && !nextTargets.isEmpty() && nextTargets.get(0) != target)
						{
							notifyThat(owner.getOperator().getOperatorId()).missedSwitch();
						}
					}
				}
				
				if (remove)
				{
					logger.debug("Removing tuple "+ts+" from op queue.");
					dt = opQueue.remove(dt.getPayload().timestamp);
				}
				else
				{
					logger.debug("Failed to send tuple "+dt.getPayload().timestamp +" to "+target);
				}
			}
			else
			{
				logger.debug("Sending constrained tuple "+ ts);
				boolean allSucceeded = true;
				//Skip first target here, it's just an indicator the remaining targets are constrained.
				for (int i = 1; i < targets.size(); i++)
				{
					Integer target = targets.get(i);
					// if not in session log for target
					if (!workers.get(target).inSessionLog(ts))
					{
						//Don't retransmit if optimizing replay and in alives.
						int downOpId = owner.getOperator().getOpContext().getDownOpIdFromIndex(target);
						if (optimizeReplay && isDownAlive(downOpId, ts))
						{
							logger.debug("Skipping recovery for "+ts+", adding to shared replay:"+sharedReplayLog.keys());
							//Might need to resend if an alive is removed.
							sharedReplayLog.add(dt);
						}
						else
						{
							logger.info("Coordination failure, recovering "+ts+" to "+downOpId);
							allSucceeded = allSucceeded && workers.get(target).trySend(dt, SEND_TIMEOUT);
						}
					}
				}
				
				if (allSucceeded)
				{
					logger.debug("All succeeded, removing tuple "+dt.getPayload().timestamp);
					opQueue.remove(dt.getPayload().timestamp);
				}
			}
		}
		
		//Convention: If the routing is constrained, ignore the first
		//target and route to all the remaining targets.
		//TODO: Will probably break once we have multiple logical outputs, but ok
		//for now.
		private boolean isConstrainedRoute(ArrayList<Integer> targets)
		{
			boolean constrainedRoute = targets != null && targets.size() > 1;
			if (constrainedRoute && !downIsMultiInput) { throw new RuntimeException("Logic error."); }
			return constrainedRoute;
		}
		
		//If safe to trim batch or already exists downstream, then don't try to send.
		//Special case if down op is multi-input, might need to send even if alive
		//downstream in order to recover from a coordination failure.
		//However, if this batch isn't being routed according to constraints,
		//it will be safe to just add to the shared replay log.
		private boolean shouldTrySend(long ts, FailureCtrl fctrl, boolean constrainedRoute)
		{
			if (fctrl.lw() >= ts || fctrl.acks().contains(ts) || 
					(optimizeReplay && fctrl.alives().contains(ts) && 
							(!downIsMultiInput || !constrainedRoute)))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}
	

	
	public class DispatcherWorker implements Runnable
	{
		private final Exchanger<DataTuple> exchanger = new Exchanger<>();
		private final OutputQueue outputQueue;
		private final EndPoint dest;
		private boolean dataConnected = false;
		
		public DispatcherWorker(OutputQueue outputQueue, EndPoint dest)
		{
			this.outputQueue = outputQueue;
			this.dest = dest;
		}
		
		public boolean isConnected()
		{
			synchronized(lock) { return dataConnected; }
		}
		
		public boolean inSessionLog(long ts)
		{
			if (!downIsMultiInput) { throw new RuntimeException("Logic error."); }
			return ((OutOfOrderBuffer)(((SynchronousCommunicationChannel)dest).getBuffer())).contains(ts);
		}
		
		public DataTuple getFromSessionLog(long ts)
		{
			if (!downIsMultiInput) { throw new RuntimeException("Logic error."); }
			BatchTuplePayload b = ((OutOfOrderBuffer)(((SynchronousCommunicationChannel)dest).getBuffer())).get(ts);
			if (b == null) { return null; }
			else
			{
				return new DataTuple(idxMapper,b.getTuple(0));
			}
		}
		
		public boolean trySend(DataTuple dt, long timeout)
		{
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
			outputQueue.reopenEndpoint(dest);
			logger.info("Dispatcher worker initial reconnect complete.");
			synchronized(lock) { dataConnected = true; }
			
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
				logger.debug("Dispatcher worker sending tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);

				//nextTuple.getPayload().instrumentation_ts=System.currentTimeMillis();
				boolean success = outputQueue.sendToDownstream(nextTuple, dest);
				if (success)
				{
					logger.debug("Dispatcher worker sent tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
				}
				else
				{
					logger.debug("Dispatcher worker failed to send tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
					//Connection must be down.
					//Remove any output tuples from this replica's output log and add them to the operator output queue.
					//This should include the current 'SEEP' batch since it might contain several tuples.
					TreeMap<Long, BatchTuplePayload> sessionLog = ((SynchronousCommunicationChannel)dest).getBuffer().trim(null);

					synchronized(lock)
					{
						dataConnected = false;
						if (failureCtrlWatchdog != null) { failureCtrlWatchdog.clear(dest.getOperatorId()); }
						
						//holding the lock
						//1) compute the new joint alives
						//2) Do a combined.set alives
						//3) Save the old alives for this downstream
						//4) delete the old alives for this downstream (TODO: What if control connection still open?)
						Set<Long> dsOpOldAlives = updateDownAlives(dest.getOperatorId(), null);
						//5) for tuple in logged
						//		if acked discard
						//		else if in joint alives add to shared replay log
						//		else add to output queue 
						//
						//		remove from the alives for the old fctrl for this downstream
						requeueTuples(sessionLog, dsOpOldAlives);
						//6) For remaining tuples in old fctrl for this downstream
						//		if tuple not acked and not in new joint alives and tuple in shared replay log
						//			move tuple from shared replay log to output queue
						logger.info("Dispatcher worker "+dest.getOperatorId()+" checking for replay from shared log after failure.");
						requeueFromSharedReplayLog(dsOpOldAlives);
						lock.notifyAll();
					}	
					
					//Update this connections routing cost 
					//TODO: Double check the downstream operator id you're using here is the same as the routing control worker
					//TODO: Should you be doing this route cost update before/after/synchronously with the replay?
					//Should you actually force close the control connection?
					owner.getOperator().getRouter().update_highestWeight(new DownUpRCtrl(dest.getOperatorId(), -1.0, null));
					//TODO: Interrupt the dispatcher thread
					
					//Reconnect synchronously (might need to add a helper method to the output queue).
					outputQueue.reopenEndpoint(dest);
					
					synchronized(lock) { dataConnected = true; }
					logger.debug("Dispatcher worker recovered from failure to send tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
				}
			}
		}
		
		/* TODO: Should be holding lock here? */
		private void requeueTuples(TreeMap<Long, BatchTuplePayload> sessionLog, Set<Long> dsOpOldAlives)
		{
			for (Map.Entry<Long, BatchTuplePayload> e : sessionLog.entrySet())
			{
				long ts = e.getKey();
				TuplePayload p = e.getValue().getTuple(0);	//TODO: Proper batches.

				if (ts > combinedDownFctrl.lw() && !combinedDownFctrl.acks().contains(ts))
				{	
					//TODO: what if acked already?
					DataTuple dt = new DataTuple(idxMapper, p);
					if (optimizeReplay && combinedDownFctrl.alives().contains(ts))
					{
						logger.info("Replay optimization: Dispatcher worker avoided retransmission from sender session log of "+ts);
						sharedReplayLog.add(dt);
					}
					else
					{
						logger.debug("Requeueing data tuple with timestamp="+p.timestamp);
						opQueue.forceAdd(dt);
					}
					
					if (optimizeReplay && dsOpOldAlives != null)
					{
						//Don't replay this twice
						dsOpOldAlives.remove(ts);
					}
				}
			}
		}
	}
	
	//Lock should be held
	private Set<Long> updateDownAlives(int dsOpId, Set<Long> newDownAlives)
	{
		Set<Long> dsOpOldAlives = null;
		if (optimizeReplay)
		{
			Set<Long> newAlives = new HashSet<>();
			if (newDownAlives != null) { newAlives.addAll(newDownAlives); }
			for (Integer id : downAlives.keySet())
			{
				if (id != dsOpId && downAlives.get(id) != null) 
				{ 
					newAlives.addAll(downAlives.get(id)); 
				}
			}

			combinedDownFctrl.setAlives(newAlives);
			dsOpOldAlives = downAlives.get(dsOpId);
			downAlives.put(dsOpId, newDownAlives);
		}
		return dsOpOldAlives;
	}
	
	/* Should be holding lock here */
	private void requeueFromSharedReplayLog(Set<Long> dsOpOldAlives)
	{
		//Replay any retracted alives that we hold in shared replay logs
		if (optimizeReplay && dsOpOldAlives != null)
		{
			for (Long oldDownAlive : dsOpOldAlives)
			{
				if (oldDownAlive > combinedDownFctrl.lw() && 
						!combinedDownFctrl.acks().contains(oldDownAlive) &&
						!combinedDownFctrl.alives().contains(oldDownAlive))
				{
					//Retraction, should schedule for replay if in shared replay log.
					DataTuple dt = sharedReplayLog.remove(oldDownAlive);
					if (dt != null) 
					{ 
						logger.info("Replay optimization: Forced to replay tuple from shared log: "+oldDownAlive);
						opQueue.forceAdd(dt); 
					}
				}			
			}
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
				int totalQueueLength = getTotalQlen();
				//TODO: This doesn't include the input queues.
				logger.info("Total queue length to "+downId + " = "+ totalQueueLength);
				//Create and send control tuple
				sendQueueLength(totalQueueLength);
				
				//wait for interval
				try {
					Thread.sleep(ROUTING_CTRL_DELAY);
				} catch (InterruptedException e) {}
			}
		}
		
		private void sendQueueLength(int queueLength)
		{
			int localOpId = owner.getOperator().getOperatorId();
			ControlTuple ct = new ControlTuple(ControlTupleType.UP_DOWN_RCTRL, localOpId, queueLength);
			logger.debug("Sending control tuple downstream from "+localOpId+" with queue length="+queueLength);
			boolean flushSuccess = owner.getOwner().getControlDispatcher().sendDownstream(ct, owner.getOperator().getOpContext().getDownOpIndexFromOpId(downId), false);
			if (!flushSuccess)
			{
				logger.warn("Failed to send control tuple, clearing routing ctrl.");
				owner.getOperator().getRouter().update_highestWeight(new DownUpRCtrl(downId, -1.0, null));
			}
		}
	}
	
	private class FailureCtrlHandler
	{		
		private final Map<Integer, Long> lastUpdateTimes = new HashMap<>();
		
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

			long rxTime = System.currentTimeMillis();
			Long lastRxTime = lastUpdateTimes.get(dsOpId);
			if (lastRxTime != null)
			{
				logger.debug("Failure ctrl update delay for "+dsOpId+"="+(rxTime - lastRxTime));
			}
			lastUpdateTimes.put(dsOpId, rxTime);
			
			synchronized(lock)
			{
				long tStart = System.currentTimeMillis();
				logger.debug("Handling failure ctrl received from "+dsOpId+",cdfctrl="+combinedDownFctrl+ ", fctrl="+fctrl);
				long oldLw = combinedDownFctrl.lw();
				long oldAcksSize = combinedDownFctrl.acks().size();
				combinedDownFctrl.update(fctrl.lw(), fctrl.acks(), null);
				boolean acksChanged = oldLw < combinedDownFctrl.lw() || oldAcksSize < combinedDownFctrl.acks().size();

				if (optimizeReplay)
				{
					if (workers.get(owner.getOperator().getOpContext().getDownOpIndexFromOpId(dsOpId)).isConnected())
					{
						Set<Long> dsOpOldAlives = updateDownAlives(dsOpId, fctrl.alives());
						logger.trace("Failure ctrl handler checking for replay from shared log.");
						requeueFromSharedReplayLog(dsOpOldAlives);
					}
				}
				
				if (acksChanged)
				{
					logger.debug("Acks changed, purging.");
					purgeSharedReplayLog();
					//TODO: Think it's ok to temporarily miss tuples being batched but not currently in log?
					purgeSenderSessionLogs();
					
					if (eagerPurgeOpQueue) { purgeOpOutputQueue(); }
				}
				
				//TODO: Get an appropriate value for this timeout.
				if (failureCtrlWatchdog != null) { failureCtrlWatchdog.reset(dsOpId, 2 * 2000); }
				lock.notifyAll();
				logger.debug("Handled failure ctrl in duration(s)="+ ((System.currentTimeMillis() - tStart)/1000));
			}
		}
		
		public void handleUpstreamFailureCtrl(FailureCtrl fctrl, int upOpId)
		{
			synchronized(lock)
			{
				combinedDownFctrl.update(fctrl.lw(), fctrl.acks(), null);
			}
		}
		
		//Lock should be held
		private void purgeSharedReplayLog()
		{
			if(optimizeReplay)
			{
				sharedReplayLog.removeOlderInclusive(combinedDownFctrl.lw());
				sharedReplayLog.removeAll(combinedDownFctrl.acks());
			}
		}
		
		//Lock should be held
		private void purgeSenderSessionLogs()
		{
			//TODO: How to trim buffer?
			FailureCtrl currentFailureCtrl = getCombinedDownFailureCtrl();
			for (int opIndex : workers.keySet())
			{
				logger.debug("Purging sender session log for "+opIndex);
				SynchronousCommunicationChannel cci = (SynchronousCommunicationChannel)owner.getPUContext().getDownstreamTypeConnection().elementAt(opIndex);
				if (cci != null)
				{
					logger.debug("Found channel for "+opIndex+" to purge.");
					IBuffer buffer = cci.getBuffer();
					buffer.trim(currentFailureCtrl);
				}
			}
		}
		
		private void purgeOpOutputQueue()
		{
			opQueue.removeOlderInclusive(combinedDownFctrl.lw()).isEmpty();
			opQueue.removeAll(combinedDownFctrl.acks()).isEmpty();
		}
	}
	
	private class FailureCtrlWatchdog
	{
		final Timer timer = new Timer(true);
		final Map<Integer, TimerTask> currentTasks = new HashMap<>();
		
		
		public void reset(final int downOpId, long delay)
		{
			clear(downOpId);
			
			TimerTask timeoutTask = new TimerTask() 
			{ 
				public void run() 
				{  
					logger.warn("Down op failure ctrl watchdog for "+downOpId+" expired.");
					synchronized(lock)
					{
						clear(downOpId);
						//TODO: What to do when a failure ctrl times out?
						//TODO: In theory this could fail to include the current
						//SEEP batch if it is multi-tuple and the dispatcher sends
						//tuples at a time - although it shouldn't really.

						//Should also clear fctrl?
						//Should get the current alives for this downstream
						//Or should it be the current combined alives?
						/*
						FailureCtrl downOpFailureCtrl = getCombinedDownFailureCtrl();
						int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
						SynchronousCommunicationChannel cci = (SynchronousCommunicationChannel)owner.getPUContext().getDownstreamTypeConnection().elementAt(downOpIndex);
						if (cci != null)
						{
							logger.debug("Found channel for "+downOpIndex+" to purge.");
							IBuffer buffer = cci.getBuffer();
							TreeMap<Long, BatchTuplePayload> delayedBatches = buffer.get(downOpFailureCtrl);
							//TODO: Should clear routing info for op perhaps?
							//Readd to output queue (N.B. While 'get' is actually 'trim' temporarily, must be careful not
							//to lose tuples here!)
							//TODO: Can perhaps reuse requeueTuples code here.
							
						}
						*/
					}
				} 
			};
			
			currentTasks.put(downOpId, timeoutTask);
			timer.schedule(timeoutTask, delay);
		}
		
		public void clear(int downOpId)
		{
			if (currentTasks.containsKey(downOpId))
			{
				currentTasks.remove(downOpId).cancel();
			}
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
		
		public void forceAdd(DataTuple dt)
		{
			synchronized(lock)
			{
				queue.put(dt.getPayload().timestamp, dt);
				lock.notifyAll();
			}
		}
		
		public DataTuple get(Long ts)
		{
			synchronized(lock)
			{
				return queue.get(ts);
			}
		}
		
		public Set<Long> keys()
		{
			synchronized(lock)
			{
				if (queue.isEmpty()) { return null; }
				else
				{
					Set<Long> result = new HashSet<>();
					result.addAll(queue.keySet());
					return result;
				}
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
		
		public DataTuple peekHead()
		{
			synchronized(lock)
			{				
				while (queue.isEmpty()) 
				{
					try { lock.wait(); } catch (InterruptedException e) {}
				}
				return queue.get(queue.firstKey());
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
