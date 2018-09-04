package uk.ac.imperial.lsds.seep.processingunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
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
import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.manet.FixedRateLimiter;
import uk.ac.imperial.lsds.seep.manet.Query;
import uk.ac.imperial.lsds.seep.manet.RateLimiter;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import static uk.ac.imperial.lsds.seep.manet.FrontierMetricsNotifier.notifyThat;

public class Dispatcher implements IRoutingObserver {

	//private final Map<Integer, DataTuple> senderQueues = new HashMap<Integer, ConcurrentNavigableMap<Integer, DataTuple>>();
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	//private static final long ROUTING_CTRL_DELAY = 1 * 50;
	//private static final long ROUTING_CTRL_DELAY = 1 * 25;
	private static final long ROUTING_CTRL_DELAY = Long.parseLong(GLOBALS.valueFor("routingCtrlDelay"));
	private static final long SEND_TIMEOUT = Long.parseLong(GLOBALS.valueFor("trySendTimeout")); // 1 * 500;
	//private static final long FAILURE_CTRL_WATCHDOG_TIMEOUT = 120 * 1000; // 4 * 1000 Only set this really high for latency breakdown.
	private static final long FAILURE_CTRL_WATCHDOG_TIMEOUT = Long.parseLong(GLOBALS.valueFor("failureCtrlTimeout"));
	private static final long FAILURE_CTRL_RETRANSMIT_TIMEOUT = Long.parseLong(GLOBALS.valueFor("retransmitTimeout"));
	private static final long TRY_SEND_ALTERNATIVES_TIMEOUT = Long.parseLong(GLOBALS.valueFor("trySendAlternativesTimeout"));
	private static final long DOWNSTREAMS_UNROUTABLE_TIMEOUT = Long.parseLong(GLOBALS.valueFor("downstreamsUnroutableTimeout"));
	private static final long ABORT_SESSION_TIMEOUT = 1000 * Long.parseLong(GLOBALS.valueFor("abortSessionTimeoutSec"));
	private static final long MAX_LOCK_WAIT = 30 * 1000; 
	private static final boolean enableDownstreamsUnroutable = Boolean.parseBoolean(GLOBALS.valueFor("enableDownstreamsUnroutable"));
	private static final boolean enableBatchRetransmitTimeouts = Boolean.parseBoolean(GLOBALS.valueFor("enableBatchRetransmitTimeouts"));
	private static final boolean piggybackControlTraffic = Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic"));
	private static final boolean enableDummies = Boolean.parseBoolean(GLOBALS.valueFor("sendDummyUpDownControlTraffic"));
	private static final boolean separateControlNet = Boolean.parseBoolean(GLOBALS.valueFor("separateControlNet"));
	private static final boolean hardReplay = Boolean.parseBoolean(GLOBALS.valueFor("enableHardReplay"));

	private final int MAX_TOTAL_QUEUE_SIZE;
	private final int GLOBAL_MAX_TOTAL_QUEUE_SIZE;
	private final int MAX_UNACKED;
	
	private final long CTRL_DELAY_WARNING = 1000;
	private final boolean bestEffort;
	private final boolean optimizeReplay;
	private final boolean eagerPurgeOpQueue;
	private final boolean downIsMultiInput;
	private final boolean boundedOpQueue;
	private final boolean limitUnacked;
	
	private final IProcessingUnit owner;
	private final Map<Integer, DispatcherWorker> workers = new HashMap<>();
	private final Set<RoutingControlWorker> rctrlWorkers = new HashSet<>();
	private final FailureCtrlHandler fctrlHandler;
	private final FailureCtrlWatchdog failureCtrlWatchdog;
	
	private ArrayList<OutputQueue> outputQueues;
	private final OperatorOutputQueue opQueue;
	private final OperatorOutputQueue sharedReplayLog;
	//private final FailureCtrl nodeFctrl = new FailureCtrl();	//KEEP THIS
	private final FailureCtrl combinedDownFctrl = new FailureCtrl();
	private final Map<Integer, Set<Long>> downAlives = new HashMap<>();	//TODO: Concurrency?
	
	private final Map<String, Integer> idxMapper = new HashMap<String, Integer>(); //Needed for replay after conn failure
	
	private final Object lock = new Object(){};
	private final int numDownstreamReplicas;
	private final boolean canRetransmitConstrained;
	private boolean downstreamsRoutable = true;
	private final boolean reportMaxSrcTotalQueueSizeTuples;
	private final boolean broadcast;

	
	public Dispatcher(IProcessingUnit owner)
	{
		this.owner = owner;
		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");		
		optimizeReplay = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay"));
		eagerPurgeOpQueue = Boolean.parseBoolean(GLOBALS.valueFor("eagerPurgeOpQueue"));
		int replicationFactor = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
		boundedOpQueue = !GLOBALS.valueFor("frontierRouting").equals("backpressure") || 
					Boolean.parseBoolean(GLOBALS.valueFor("boundFrontierRoutingQueues")) ||
					replicationFactor == 1;

		reportMaxSrcTotalQueueSizeTuples = Boolean.parseBoolean(GLOBALS.valueFor("reportMaxSrcTotalQueueSizeTuples"));

		if (TRY_SEND_ALTERNATIVES_TIMEOUT > DOWNSTREAMS_UNROUTABLE_TIMEOUT) { throw new RuntimeException("Logic error: todo."); }

		GLOBAL_MAX_TOTAL_QUEUE_SIZE = Integer.parseInt(GLOBALS.valueFor("maxTotalQueueSizeTuples"));

		if (owner.getOperator().getOpContext().isSource())
		{
			MAX_TOTAL_QUEUE_SIZE = Integer.parseInt(GLOBALS.valueFor("maxSrcTotalQueueSizeTuples"));
		}
		else
		{
			MAX_TOTAL_QUEUE_SIZE = GLOBAL_MAX_TOTAL_QUEUE_SIZE;
			//MAX_TOTAL_QUEUE_SIZE = 1;
		}
		//opQueue = new OperatorOutputQueue(Integer.MAX_VALUE);
		opQueue = new OperatorOutputQueue(MAX_TOTAL_QUEUE_SIZE);
		
		//Not really an output queue but can reuse code. Possible unnecessary lock contention?
		sharedReplayLog = new OperatorOutputQueue(Integer.MAX_VALUE);	

		for(int i = 0; i<owner.getOperator().getOpContext().getDeclaredWorkingAttributes().size(); i++){
			idxMapper.put(owner.getOperator().getOpContext().getDeclaredWorkingAttributes().get(i), i);
		}
		
		
		Query frontierQuery = owner.getOperator().getOpContext().getFrontierQuery(); 
		int logicalId = frontierQuery.getLogicalNodeId(owner.getOperator().getOperatorId());

		if (owner.getOperator().getOpContext().isSink())
		{
			downIsMultiInput = false;
			failureCtrlWatchdog = null;
			numDownstreamReplicas = 0;
			canRetransmitConstrained = true;
			broadcast = false;
		}
		else
		{
			int downLogicalId = frontierQuery.getNextHopLogicalNodeId(logicalId);
			downIsMultiInput = frontierQuery.getLogicalInputs(downLogicalId).length > 1;
			int downInputIndex = frontierQuery.getLogicalInputIndex(downLogicalId, logicalId);
			canRetransmitConstrained = downInputIndex == 0 || !Boolean.parseBoolean(GLOBALS.valueFor("restrictRetransmitConstrained"));
			broadcast = GLOBALS.valueFor("frontierRouting").equals("broadcast"); 

			logger.info("canRetransmitConstrained="+canRetransmitConstrained);
			if (bestEffort)
			{
				if (downIsMultiInput)
				{
					throw new RuntimeException("TODO: Best effort mode not supported for join operators yet.");
				}
			}
				
			numDownstreamReplicas = frontierQuery.getPhysicalNodeIds(downLogicalId).size();
			if (Boolean.parseBoolean(GLOBALS.valueFor("enableFailureCtrlWatchdog")) && 
					numDownstreamReplicas > 1 && !bestEffort)
			{
				failureCtrlWatchdog = new FailureCtrlWatchdog();
			}
			else 
			{ 
				failureCtrlWatchdog = null; 
			}	
		}

		if (!bestEffort) { fctrlHandler = new FailureCtrlHandler(); }
		else { fctrlHandler = null; }


		//boolean hasJoin = frontierQuery.getJoinOpLogicalNodeIds().size() > 1;
		boolean hasJoin = frontierQuery.getJoinOpLogicalNodeIds().size() >= 1; //Temp force for VC exps
		//if (owner.getOperator().getOpContext().isSource() && !bestEffort && replicationFactor > 1 && hasJoin)  
		if (owner.getOperator().getOpContext().isSource() && !bestEffort && hasJoin) //Temp force for VC k=1
		{ limitUnacked = true; }
		else { limitUnacked = false; }
		MAX_UNACKED = GLOBAL_MAX_TOTAL_QUEUE_SIZE-1;
		logger.info("limitUnacked="+limitUnacked+",maxUnacked="+MAX_UNACKED);
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
		int opId = owner.getOperator().getOperatorId();
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
			//copy = new FailureCtrl(combinedDownFctrl);
			copy = combinedDownFctrl.copy();
		}
		return copy;
	}

	public void setDownstreamsRoutable(boolean newValue)
	{
		if (!enableDownstreamsUnroutable || 
			owner.getOperator().getOpContext().isSource() || 
			owner.getOperator().getOpContext().isSink()) 
		{ return; }

		synchronized(lock)
		{
			if (newValue != downstreamsRoutable) { logger.debug("Changing "+owner.getOperator().getOperatorId()+ " downstreamsRoutable="+newValue); }
			downstreamsRoutable = newValue;
		}
		if (downIsMultiInput)
		{
			throw new RuntimeException("TODO: Check this works with multi-input ops.");
		}
	}
	public boolean areDownstreamsRoutable()
	{
		synchronized(lock) { return downstreamsRoutable; }
	}
	
	public void dispatch(DataTuple dt) 
	{
		long ts = dt.getPayload().timestamp;
		if (limitUnacked)
		{
			long lastSendSuccess = System.currentTimeMillis();
			while (getCombinedDownFailureCtrl().unacked(ts) >= MAX_UNACKED)
			{
				synchronized(lock)
				{	
					try { lock.wait(MAX_LOCK_WAIT); } catch (InterruptedException e) {}
				}

				if (owner.getOperator().getOpContext().isSource() && System.currentTimeMillis() - lastSendSuccess > ABORT_SESSION_TIMEOUT)
				{
					logger.error("Abort session timeout exceeded");
					System.exit(1);	
				} 
			}
		}

		long local_ts = dt.getPayload().local_ts;
		logger.debug("Dispatcher queuing tuple with ts="+ts+",local latency="+(System.currentTimeMillis()-local_ts)); 
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
			if (combinedDownFctrl.isAcked(ts))
			{
				//Acked already, discard.
				return;	
			}
			if (!(optimizeReplay && ((broadcast && isAllDownAlive(ts)) || (!broadcast && combinedDownFctrl.isAlive(ts)))))
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
				sharedReplayLog.add(dt);
				logger.info("Dispatcher avoided sending live tuple: "+ts+",srl.size="+sharedReplayLog.size());
			}
		}
	}
	
	public int getTotalQlen()
	{
		if (owner.getOperator().getOpContext().isSource()) 
		{ 
			if (reportMaxSrcTotalQueueSizeTuples)
			{
				return MAX_TOTAL_QUEUE_SIZE;
			}
			else
			{
				return GLOBAL_MAX_TOTAL_QUEUE_SIZE;
			}
		}
		else { return opQueue.size(); }
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
							if (canRetransmitConstrained && 
									(!optimizeReplay || 
									(optimizeReplay && !isDownAlive(downOpId, ts))))
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
		}	
		long tNotifyStart = System.currentTimeMillis();
		synchronized(lock) { lock.notifyAll(); }

		long tEnd = System.currentTimeMillis();
		String logMsg = "Finished handling routing change, duration=" + (tEnd - tStart) + ", notify=" + (tEnd - tNotifyStart);
		if (tEnd - tStart > CTRL_DELAY_WARNING) { logger.warn("HIGH CTRL DELAY: "+ logMsg); } else { logger.debug(logMsg); }
	}
	
	private void notifyRoutingFailed(int downOpId)
	{
			//N.B. Probably shouldn't be holding any locks here?
			if (owner.getOwner().getUpstreamRoutingController() != null)
			{
				owner.getOwner().getUpstreamRoutingController().handleDownFailed(downOpId);
			}
			else
			{
				//owner.getOperator().getRouter().update_downFailed(dest.getOperatorId());
				owner.getOperator().getRouter().update_downFailed(downOpId);
			}
	}

	private boolean isDownAlive(int downOpId, long ts)
	{
		synchronized(lock) { return downAlives.get(downOpId) != null && downAlives.get(downOpId).contains(ts); }
	}

	private boolean isAllDownAlive(long ts)
	{
		synchronized(lock) 
		{ 
			for (Integer downOpId : downAlives.keySet()) 
			{ 
				if (!isDownAlive(downOpId, ts)) { return false; }
			}

			return true;
		}
	}
	
	public void stop(int target) { throw new RuntimeException("TODO"); }
	
	
	public class DispatcherMain implements Runnable
	{
		private long lastSendSuccess= -1; //Use to abort if can't send for ages
		public DispatcherMain() {}
		public void run()
		{
			try
			{
				lastSendSuccess = System.currentTimeMillis();
				while(true)
				{
					Set<Long> constraints = dispatchHead();
					
					if (constraints != null && !constraints.isEmpty())
					{
						dispatchConstrained(constraints);
					}

					if (owner.getOperator().getOpContext().isSource() && System.currentTimeMillis() - lastSendSuccess > ABORT_SESSION_TIMEOUT)
					{
						logger.error("Abort session timeout exceeded");
						System.exit(1);	
					} 
				}
			}
			catch(Exception e) { 
				e.printStackTrace();
				logger.error("Fatal error - aborting: "+e); System.exit(1); }
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
				long localLatency = System.currentTimeMillis() - dt.getPayload().local_ts;
				logger.debug("Dispatcher peeked at head tuple with ts="+dt.getPayload().timestamp + ", targets "+targets+", local latency="+localLatency);

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
							try { lock.wait(MAX_LOCK_WAIT); } catch (InterruptedException e) {}
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
				if (combinedDownFctrl.isAcked(ts))
				{
					boolean removed = opQueue.remove(ts) != null;
					if (removed) { setDownstreamsRoutable(true); }
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
				if (dt != null) { setDownstreamsRoutable(true); }
				
				//Alive downstream, so don't discard in case we need
				//to replay if the downstream fails.
				if (fctrl.isAlive(ts) && dt != null)
				{
					sharedReplayLog.add(dt);
					logger.info("admitBatch: replay optimization: dispatcher avoided replaying tuple "+ts+",srl.size="+sharedReplayLog.size());
				}
				
				return false;
			}
			
			return true;
		}
		
		private void sendBatch(DataTuple dt, ArrayList<Integer> targets)
		{
			if (targets == null) { throw new RuntimeException("Logic error."); }
			if (isConstrainedRoute(targets)) { sendBatchConstrainedRoute(dt, targets); }
			else { sendBatchUnconstrainedRoute(dt, targets); }
		}
	
		private void sendBatchUnconstrainedRoute(DataTuple dt, ArrayList<Integer> targets)
		{
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
			logger.debug("Sending unconstrained tuple "+ ts);
			//Could already be in session log if sending to multi-input op.
			boolean remove = false;
			if (downIsMultiInput && workers.get(targets.get(0)).inSessionLog(ts))
			{
				logger.info("Skipping unconstrained transmission of "+ts+", already in session log");
				remove = true;
				// In particular, setDownstreamsRoutable (false) will be overridden by this when retransmitting!
				//throw new RuntimeException("TODO: Refactor/check this makes sense wrt the alternatives retry stuff below.");
				opQueue.remove(ts);	//TODO: This could be racy if remove and it gets readded in the meantime?
			}
			else
			{
				int targetsTried = 0;
				boolean preventOqDupes = true;
				for (Integer target : targets)
				{	
					boolean oqDupeBackoff = false;
					if (!(preventOqDupes && workers.get(target).inSessionLog(ts, true)))
					{
						remove = workers.get(target).trySend(dt, SEND_TIMEOUT);
						//notifyThat(owner.getOperator().getOperatorId()).triedSend();
					}
					else if (preventOqDupes) 
					{ 
						logger.debug("Avoided oq dupe for "+ts+ " to " + target); 
						oqDupeBackoff = true;
					}

					long localLatency = System.currentTimeMillis() - dt.getPayload().local_ts;
					//boolean trySendAlternativesOnFail = getLocalLatency(dt) > trySendAlternativesThreshold
					if (remove) {/* notifyThat(owner.getOperator().getOperatorId()).sendSucceeded();*/ }
					if (remove || localLatency < TRY_SEND_ALTERNATIVES_TIMEOUT) 
					{ 
						if (oqDupeBackoff) 
						{ 
								try { Thread.sleep(SEND_TIMEOUT); } catch (InterruptedException e) {}
							/*
							synchronized(lock) 
							{ 
								//Backoff in cases where we didn't get anywhere.
								try { lock.wait(SEND_TIMEOUT); } catch (InterruptedException e) {}
							} 
							*/
						}
						break; 
					}
					else
					{
						targetsTried++;
						if (targetsTried >= targets.size() && localLatency > DOWNSTREAMS_UNROUTABLE_TIMEOUT) { setDownstreamsRoutable(false); } 
						else if (targetsTried >= targets.size() && oqDupeBackoff) 
						{ 
								try { Thread.sleep(SEND_TIMEOUT); } catch (InterruptedException e) {}
						}

						if (targets.size() > 1)
						{
							logger.debug("Failed to send tuple "+dt.getPayload().timestamp +" to "+target+", local latency="+localLatency+", trying alternatives:"+targets);
						}
					}
				}
				if (remove) { logger.debug("Suceeded sending ts="+ts+", targetsTried="+targetsTried); }
			}
			
			if (remove)
			{
				logger.debug("Removing tuple "+ts+" from op queue.");
				//dt = opQueue.remove(dt.getPayload().timestamp);
				//if (dt != null) { setDownstreamsRoutable(true); }
				if (!opQueue.contains(dt.getPayload().timestamp)) { setDownstreamsRoutable(true); }
				lastSendSuccess = System.currentTimeMillis();
			}
			else
			{
				int otherTargets = targets.size() - 1; 
				logger.debug("Failed to send tuple "+dt.getPayload().timestamp +" to "+targets.get(0)+", other active targets="+otherTargets);
			}
		}

		private void sendBatchConstrainedRoute(DataTuple dt, ArrayList<Integer> targets)
		{
			long ts = dt.getPayload().timestamp;
			logger.debug("Sending constrained tuple "+ ts);
			boolean allSucceeded = true;

			// Remove first to avoid removing a tuple later that was readded concurrenlty by a retransmit timeout.
			//Readd at the end if not all suceeded.
			opQueue.remove(ts);
			
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
						logger.debug("Skipping recovery for "+ts+", adding to shared replay (srl.sz="+sharedReplayLog.size()+"):"+sharedReplayLog.keys());
						//Might need to resend if an alive is removed.
						sharedReplayLog.add(dt);
					}
					else
					{
						logger.info("Coordination failure, recovering "+ts+" to "+downOpId);
						boolean downSucceeded = workers.get(target).trySend(dt, SEND_TIMEOUT, false);
						if (downSucceeded) 
						{ 
							lastSendSuccess = System.currentTimeMillis(); 
							setDownstreamsRoutable(true);
						}
						allSucceeded = allSucceeded && downSucceeded; 
					}
				}
			}
			
			if (allSucceeded)
			{
				logger.debug("All succeeded, removing tuple "+dt.getPayload().timestamp);
				//opQueue.remove(dt.getPayload().timestamp);
				//throw new RuntimeException("TODO: Changed trySend, need to update this.13/09/16");
			}
			else
			{
				logger.debug("Not all succeeded, readding tuple "+dt.getPayload().timestamp);
				opQueue.forceAdd(dt); 
			}
		}
	
		//Convention: If the routing is constrained, ignore the first
		//target and route to all the remaining targets.
		//TODO: Will probably break once we have multiple logical outputs, but ok
		//for now.
		private boolean isConstrainedRoute(ArrayList<Integer> targets)
		{
			boolean constrainedRoute = targets != null && targets.size() > 1 && targets.get(0) < 0;
			if (constrainedRoute && !broadcast && !downIsMultiInput) { throw new RuntimeException("Logic error."); }
			return constrainedRoute;
		}
		
		//If safe to trim batch or already exists downstream, then don't try to send.
		//Special case if down op is multi-input, might need to send even if alive
		//downstream in order to recover from a coordination failure.
		//However, if this batch isn't being routed according to constraints,
		//it will be safe to just add to the shared replay log.
		private boolean shouldTrySend(long ts, FailureCtrl fctrl, boolean constrainedRoute)
		{
			if (broadcast && downIsMultiInput) { throw new RuntimeException("TODO: Fix flag in condition below."); }
			if (fctrl.isAcked(ts)|| 
					(optimizeReplay && 
					 ((!broadcast && fctrl.isAlive(ts)) || (broadcast && isAllDownAlive(ts))) && 
						(!downIsMultiInput || (!broadcast && !constrainedRoute))))
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
		private boolean fctrlHardTimedOut = false;
		private final RateLimiter rateLimiter = new FixedRateLimiter();
		
		public DispatcherWorker(OutputQueue outputQueue, EndPoint dest)
		{
			this.outputQueue = outputQueue;
			this.dest = dest;
			if (Boolean.parseBoolean(GLOBALS.valueFor("rateLimitConnections")))
			{
				rateLimiter.setLimit(Long.parseLong(GLOBALS.valueFor("frameRate")));
			}
		}
		
		public void notifyFctrlHardTimedOut() { synchronized(lock) { fctrlHardTimedOut = true; } logger.debug("Set fcht for "+dest.getOperatorId()); }
		private boolean clearFctrlHardTimedOut() 
		{ 
			logger.debug("Clearing fcht for "+dest.getOperatorId());
			synchronized(lock) 
			{ 
				boolean prev = fctrlHardTimedOut;
				fctrlHardTimedOut = false;
				return prev;
			}
		}

		public boolean isConnected()
		{
			synchronized(lock) { return dataConnected; }
		}
		
		public boolean inSessionLog(long ts, boolean downIsUnaryOK)
		{
			if (!downIsUnaryOK && !downIsMultiInput && !broadcast) { throw new RuntimeException("Logic error."); }
			return ((OutOfOrderBuffer)(((SynchronousCommunicationChannel)dest).getBuffer())).contains(ts);

		}

		public boolean inSessionLog(long ts)
		{
			return inSessionLog(ts, false);
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
		
		public boolean trySend(DataTuple dt, long timeout) { return trySend(dt, timeout, true); }
		public boolean trySend(DataTuple dt, long timeout, boolean removeAndReadd)
		{
			try {
				if (removeAndReadd) { opQueue.remove(dt.getPayload().timestamp); }
				exchanger.exchange(dt, timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | TimeoutException e) {
				if (removeAndReadd) { opQueue.forceAdd(dt); }
				return false;
			}
			setDownstreamsRoutable(true);
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
				long localLatency = System.currentTimeMillis() - nextTuple.getPayload().local_ts;
				logger.debug("Dispatcher worker sending tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp+", local latency="+localLatency);
				
				rateLimiter.limit();

				//nextTuple.getPayload().instrumentation_ts=System.currentTimeMillis();
				if (fctrlHandler != null) { fctrlHandler.addBatchRetransmitTimer(dest.getOperatorId(), nextTuple.getPayload().timestamp, System.currentTimeMillis()); }

				//If there has been a hard timeout since the last send
				if (clearFctrlHardTimedOut())
			 	{ 
					preSendFctrlHardTimeoutCleanup(nextTuple);	
					continue; 
				}

				boolean success = outputQueue.sendToDownstream(nextTuple, dest);

				//If there has been a hard timeout since the send started
				if (clearFctrlHardTimedOut()) 
				{ 
					postSendFctrlHardTimeoutCleanup(nextTuple, success); 
					continue;
				} 

				//Otherwise no hard timeout since the last send
				if (success)
				{
					logger.debug("Dispatcher worker sent tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);

				}
				else
				{
					logger.warn("Dispatcher worker failed to send tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
					postConnectionFailureCleanup();
					logger.warn("Dispatcher worker recovered from failure to send tuple to downstream: "+dest.getOperatorId()+",dt="+nextTuple.getPayload().timestamp);
				}
			}
		}
	
		private void preSendFctrlHardTimeoutCleanup(DataTuple dt)
		{
			//TODO: Should the below all be wrapped in data connected = false ; data connected = true?
			//The implications of that are that if a failure ctrl arrives while not yet connected its alives 
			//will be ignored (should only matter for non-piggybacked)?. On the other hand I can't see any downsides if you don't wrap it, 
			//although there might be interleaving with sending etc. Need to think as well about what would
			//happen with non-piggybacked. 
			
			//Firstly readd the tuple to the output queue.
			long ts = dt.getPayload().timestamp;
			TreeMap<Long, BatchTuplePayload> sessionLog = ((SynchronousCommunicationChannel)dest).getBuffer().trim(null);
			if (sessionLog.size() > 1 || (sessionLog.size() == 1 && !sessionLog.containsKey(ts))) { throw new RuntimeException("Logic error, ts=: "+ts+", sessionLog="+sessionLog); }
			opQueue.forceAdd(dt);

			if (piggybackControlTraffic) 
			{
				//reopen endpoint
				logger.debug("pre send fctrl hard timeout cleanup for "+dest.getOperatorId()+",ts="+ts);
				outputQueue.reopenEndpoint(dest);
			}
			else
			{
				//	TODO: Don't want to spuriously reopen?
				//  Normally if the control conn fails first you might not actually
				//  do the reopen until after, and so there will be a failure but 
				//  you'll be guaranteed to do the reconnect at least. The only problem
				//  is you'll end up clearing the fctrls in the course of that most likely
				//  (i.e. spurious retractions). In any event, it should therefore be ok
				//  to do a reconnect if set since that's what you'll be doing anyway.
				//  If on the other hand the data send fails first you'll be able to clear the
				//  flag afterward if the hard cleanup has happened already. You'll just need
				//  to make sure you readd the tuple before reopening the endpoint.
				throw new RuntimeException("TODO");
			}
			//TODO: Go over connection failure cleanup to see if anything else is missing
		}	

		private void postSendFctrlHardTimeoutCleanup(DataTuple dt, boolean success)
		{
			//Sanity check: Should be *at most* one tuple (this one) in the log.
			long ts = dt.getPayload().timestamp;
			TreeMap<Long, BatchTuplePayload> sessionLog = ((SynchronousCommunicationChannel)dest).getBuffer().trim(null);
			if (sessionLog.size() > 1 || (sessionLog.size() == 1 && !sessionLog.containsKey(ts))) { throw new RuntimeException("Logic error, ts=: "+ts+", sessionLog="+sessionLog); }
			opQueue.forceAdd(dt);

			//TODO: What to do with failure ctrl watchdog in terms of when to clear/reset it?
			//N.B. Flag should be set by failure ctrl timeout *before* beginning to cleanup.
			//TODO: Should perhaps clear the log of this tuple so that it doesn't get dropped as a dupe
			//when retransmitted?
			if (piggybackControlTraffic)
			{
				if (success)
				{
					//		TODO: Ok to just clear hard here? To be honest this case is possible but shouldn't really happen,
					//		so possibly not worth worrying about in terms of performance. Problem is if fails afterward then
					//		when we try to send again we'll get a spurious failure because of a connection number mismatch.
					//		What we could do perhaps is reset the failure ctrl watchdog? I guess there is a disconnect between
					//		when we do a hard cleanup and when a reconnect happens. Actually, it might be better to just *Not*
					//		clear the flag, then presume that the control has expired (because of the hard reset) and so we
					//		won't get any more control until after we've reconnected. In which case we'll just end up calling 
					//		reopen the next time we wend a tuple. This would be nice but need to be careful to perhaps clear the
					//		log before readding the tuple? 
					//		
					//		On further reflection the most likely thing to have happened here is a spurious/too aggressive fctrl hard
					//		timeout where the connection recovers. 
					//		I think the think to do is just call reopen after the next connection. If the connection never actually
					//		dies then we won't need to reconnect. If it does die then calling reopen will update the connection number
					//		to the appropriate value. If we do reopen now and then it fails subsequently we'll end up reopening later
					//		anyway. In fact maybe that's all we should be doing when piggybacking? 
					//
					notifyFctrlHardTimedOut();
					logger.debug("post send fctrl hard timeout cleanup for "+dest.getOperatorId()+",ts="+ts+",success=true");
				}
				else
				{
					//		Readd tuple then do reopen to update connection number in output queue. N.B. need to clear log of this
					//		tuple too in case it gets squashed subsequently as a dupe.
					logger.debug("post send fctrl hard timeout cleanup for "+dest.getOperatorId()+",ts="+ts+",success=false");
					outputQueue.reopenEndpoint(dest);
				}
			} 
			else 
			{
				if (success)
				{	
					//		clear log and readd tuple but don't clear flag or reopen endpoint? Alternatively could clear flag and
					//		reopen endpoint, and be guaranteed to be all set later? Problem is if control connects but data hasn't 
					//		connected yet could end up routing data to this downstream without a valid connection - so yes think
					//		this should perhaps block reopening and clear flag. Maybe a downside is that we'll unnecessarily close
					//		a connection in some cases. On the other hand if we don't then when there is actually a failure subsequently
					//		we'll have a second spurious reconnect if we reopen later.
				}
				else
				{
					//		clear log, readd tuple don't see any downside here to clearing flag and blocking reopening. 
					//
					//	TODO: Perhaps only need to readd tuple here if in log since otherwise it must have been readded by hard cleanup?
					//	Might be unnecessarily risky.
				}
				throw new RuntimeException("TODO");
			}
			//TODO: Go over connection failure cleanup to see if anything else is missing
		}

		private void postConnectionFailureCleanup()
		{
			long tStart = System.currentTimeMillis();
			logger.debug("Starting post connection failure cleanup for "+dest.getOperatorId());
			//Connection must be down.
			//Remove any output tuples from this replica's output log and add them to the operator output queue.
			//This should include the current 'SEEP' batch since it might contain several tuples.
			//N.B. Important to remove from buffer/session log before adding to output queue since the dispatcher main could otherwise think a tuple is in the session log
			//and delete it from its output queue.
			TreeMap<Long, BatchTuplePayload> sessionLog = ((SynchronousCommunicationChannel)dest).getBuffer().trim(null);

			boolean doUpdateDownAlives = !(piggybackControlTraffic && outputQueue.checkEndpoint(dest));
			synchronized(lock)
			{
				dataConnected = false;
				if (failureCtrlWatchdog != null) { failureCtrlWatchdog.clear(dest.getOperatorId()); }
				if (fctrlHandler != null) { fctrlHandler.clearBatchRetransmitTimers(dest.getOperatorId()); }
				
				//holding the lock
				//1) compute the new joint alives
				//2) Do a combined.set alives
				//3) Save the old alives for this downstream
				//4) delete the old alives for this downstream (TODO: What if control connection still open?)
				Set<Long> dsOpOldAlives = doUpdateDownAlives? updateDownAlives(dest.getOperatorId(), null) : null;
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
			//owner.getOperator().getRouter().update_downFailed(dest.getOperatorId());
			notifyRoutingFailed(dest.getOperatorId());
			//TODO: Interrupt the dispatcher thread
			
			//Reconnect synchronously (might need to add a helper method to the output queue).
			//If piggybacking, will instead
			if (!piggybackControlTraffic) { outputQueue.reopenEndpoint(dest); }
			//TODO: N.B. complete hack, write this in a more understandable manner.
			//Essentially, if piggybacking want to get rid of any connection number mismatch when we next
			//try to send a tuple after a reconnect.
			else 
			{  
				notifyFctrlHardTimedOut(); 
				if (failureCtrlWatchdog != null && !doUpdateDownAlives) 
				{ 
					synchronized(lock) { failureCtrlWatchdog.reset(dest.getOperatorId(), FAILURE_CTRL_WATCHDOG_TIMEOUT);}
				}
			}
			
			synchronized(lock) { dataConnected = true; }
			logger.debug("Completed post connection failure cleanup for "+dest.getOperatorId()+" in "+(System.currentTimeMillis()-tStart));
		}



	}
	
		/* TODO: Should be holding lock here? */
		private void requeueTuples(TreeMap<Long, BatchTuplePayload> sessionLog, Set<Long> dsOpOldAlives)
		{
			for (Map.Entry<Long, BatchTuplePayload> e : sessionLog.entrySet())
			{
				long ts = e.getKey();
				TuplePayload p = e.getValue().getTuple(0);	//TODO: Proper batches.

				if (!combinedDownFctrl.isAcked(ts))
				{	
					//TODO: what if acked already?
					DataTuple dt = new DataTuple(idxMapper, p);
					if (optimizeReplay && !broadcast && combinedDownFctrl.isAlive(ts))
					{
						sharedReplayLog.add(dt);
						logger.info("Replay optimization: Dispatcher worker avoided retransmission from sender session log of "+ts+",srl.sz="+sharedReplayLog.size());
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

	private Set<Long> getRetractions(Set<Long> dsOpOldAlives)
	{
		Set<Long> retractions = new HashSet<>();
		if (dsOpOldAlives != null)
		{
			for (Long id : dsOpOldAlives)
			{
				if (!combinedDownFctrl.isAcked(id) && !combinedDownFctrl.isAlive(id))
				{
					retractions.add(id);
				}
			}		
		}
		return retractions;
	}

	//Assumes you've already trimmed buffer, updated combinedDownFctrl, and lock is held.
	public void requeueRetractedTuples(Set<Long> retracted)
	{
		Vector<EndPoint> endpoints = owner.getPUContext().getDownstreamTypeConnection();

		for (Long ts : retracted)
		{
			if (!combinedDownFctrl.isAcked(ts) && ((!broadcast && !combinedDownFctrl.isAlive(ts)) || (broadcast && !isAllDownAlive(ts))))
			{
				for (EndPoint endpoint : endpoints)
				{
					BatchTuplePayload btp = ((SynchronousCommunicationChannel)endpoint).getBuffer().get(ts);
					if (btp != null)
					{
						TuplePayload p = btp.getTuple(0);	//TODO: Proper batches.

						//TODO: what if acked already?
						DataTuple dt = new DataTuple(idxMapper, p);
						if (optimizeReplay && ((!broadcast && combinedDownFctrl.isAlive(ts)) || (broadcast && isAllDownAlive(ts))))
						{
							logger.info("Replay optimization: Dispatcher worker avoided retransmission from sender session log of "+ts);
							sharedReplayLog.add(dt);
						}
						else
						{
							logger.debug("Requeueing retracted data tuple with ts="+p.timestamp);
							opQueue.forceAdd(dt);
						}
						break;	//Don't bother looking at other buffers if found in this one.
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
		if (optimizeReplay && dsOpOldAlives != null && !sharedReplayLog.isEmpty())
		{
			logger.debug("Requeuing from shared replay log with dsooa.size="+dsOpOldAlives.size()+",srl.size="+sharedReplayLog.size());
			if (sharedReplayLog.size() > dsOpOldAlives.size())
			{
				for (Long oldDownAlive : dsOpOldAlives)
				{
					if (!combinedDownFctrl.isAcked(oldDownAlive) && ((!broadcast && !combinedDownFctrl.isAlive(oldDownAlive)) || (broadcast && !isAllDownAlive(oldDownAlive))))
					{
						moveSharedReplayToOpQueue(oldDownAlive);
					}			
				}
			}
			else
			{
				for (Long srlEntry : sharedReplayLog.keys())
				{
					if (combinedDownFctrl.isAcked(srlEntry)) { sharedReplayLog.remove(srlEntry); }
					else if (dsOpOldAlives.contains(srlEntry) && ((!broadcast && !combinedDownFctrl.isAlive(srlEntry)) || (broadcast && !isAllDownAlive(srlEntry))))
					{ moveSharedReplayToOpQueue(srlEntry); }
				}
			}
		}
	}

	//Assumes lock held	
	private void moveSharedReplayToOpQueue(Long id)
	{
		//Retraction, should schedule for replay if in shared replay log.
		DataTuple dt = sharedReplayLog.remove(id);
		if (dt != null) 
		{ 
			logger.info("Replay optimization: Forced to replay tuple from shared log: "+id);
			opQueue.forceAdd(dt); 
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
			long tLast = System.currentTimeMillis();
			while (true)
			{
				int totalQueueLength = getTotalQlen();
				//TODO: This doesn't include the input queues.
				logger.debug("Total queue length to "+downId + " = "+ totalQueueLength);
				
				long tStart = System.currentTimeMillis();
				//Create and send control tuple
				sendQueueLength(totalQueueLength);
				long tEnd = System.currentTimeMillis();
				logger.debug("Sent updown rctrl in "+(tEnd - tStart)+"ms, since last="+(tEnd-tLast)+" ms");
				tLast = tEnd;
				//wait for interval
				long waited = System.currentTimeMillis()+1 - tEnd;
				while (waited < ROUTING_CTRL_DELAY)
				{
					try {
						Thread.sleep(ROUTING_CTRL_DELAY - waited);
					} catch (InterruptedException e) {}
					waited = System.currentTimeMillis()+1 - tEnd;
				}
			}
		}
		
		private void sendQueueLength(int queueLength)
		{
			int localOpId = owner.getOperator().getOperatorId();
			ControlTuple ct = new ControlTuple(ControlTupleType.UP_DOWN_RCTRL, localOpId, queueLength);
			logger.debug("Sending control tuple downstream from "+localOpId+" with queue length="+queueLength);
			/*
			if (localOpId == 0 && downId != -2 || 
				localOpId == 10 && downId != -190 ||
				localOpId == 11 && downId != -189)
			{ return; }
			*/
			boolean flushSuccess = false;
			int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downId);
			if (!piggybackControlTraffic)
			{
				flushSuccess = owner.getOwner().getControlDispatcher().sendDownstream(ct, downOpIndex, false);
			}
			else
			{
				//TODO What if this fails?
				flushSuccess = outputQueues.get(downOpIndex).sendToDownstream(ct);
			}

			if (separateControlNet && enableDummies) { owner.getOwner().getControlDispatcher().sendDummyDownstream(ct, downOpIndex); }

			if (!flushSuccess)
			{
				logger.warn("Failed to send control tuple, clearing routing ctrl.");
				//owner.getOperator().getRouter().update_downFailed(downId);
				notifyRoutingFailed(downId);
			}
		}
	}

	private class FailureCtrlHandler
	{		
		private final Map<Integer, Long> lastUpdateTimes = new HashMap<>();
		private final Map<Integer, Map<Long,Long>> batchRetransmitTimers= new HashMap<Integer, Map<Long,Long>>();

		public FailureCtrlHandler()
		{
			for(Integer downOpId : owner.getOperator().getOpContext().getDownstreamOpIdList())
			{
				batchRetransmitTimers.put(downOpId, new HashMap<Long,Long>());
			}
		}

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
				if (failureCtrlWatchdog != null) { failureCtrlWatchdog.clear(dsOpId); }	//In case it takes a while to process this failure ctrl.
				logger.debug("Handling failure ctrl received from "+dsOpId+",cdfctrl="+combinedDownFctrl+ ", fctrl="+fctrl);
				logger.trace("Failure ctrl handling acquired lock in "+(tStart - rxTime)+" ms");
				long oldLw = combinedDownFctrl.lw();
				long oldAcksSize = combinedDownFctrl.acks().size();
				//TODO: This is pretty racey, shouldn't expose lw or size? Or should make an atomic copy?
				//combinedDownFctrl.update(fctrl.lw(), fctrl.acks(), null);
				combinedDownFctrl.update(fctrl, false);
				boolean acksChanged = oldLw < combinedDownFctrl.lw() || oldAcksSize < combinedDownFctrl.acks().size();
				long tAcks = System.currentTimeMillis();
				logger.trace("Failure ctrl handling updated acks in "+(tAcks - tStart)+" ms");
				if (optimizeReplay)
				{
					//TODO: Could this check be leading to spurious retractions (or even just delay recovery from a connection failure unnecessarily?)
					//Think just the latter, and probably not too bad now given that independent of whether piggybacking the actual reconnect/reopen should be quick. 
					//On the other hand removing it might slow upstreams from detecting failures/retractions, although probably not.
					if (workers.get(owner.getOperator().getOpContext().getDownOpIndexFromOpId(dsOpId)).isConnected())
					{
						Set<Long> dsOpOldAlives = updateDownAlives(dsOpId, fctrl.alives());
						long tDownAlives = System.currentTimeMillis();
						logger.trace("Failure ctrl handler checking for replay from shared log.");
						requeueFromSharedReplayLog(dsOpOldAlives);
						long tRequeueShared = System.currentTimeMillis();
			
						Set<Long> retractions = getRetractions(dsOpOldAlives);
						long tGetRetractions = System.currentTimeMillis();
						if (!retractions.isEmpty()) 
						{ 
							logger.info("Downstream "+dsOpId+" retractions:"+retractions); 
							requeueRetractedTuples(retractions);
						}
						long tRequeueRetracted = System.currentTimeMillis();
						logger.debug("Failure ctrl handler da="+(tDownAlives-tAcks)+",trs="+(tRequeueShared-tDownAlives)+",tgr="+(tGetRetractions-tRequeueShared)+",trr="+(tRequeueRetracted-tGetRetractions));
					}
				}
				long tOpt = System.currentTimeMillis();
				logger.debug("Failure ctrl handling optimized replay in "+(tOpt - tAcks)+" ms");
				
				if (acksChanged)
				{
					logger.debug("Acks changed, purging.");
					purgeSharedReplayLog();
					//TODO: Think it's ok to temporarily miss tuples being batched but not currently in log?
					purgeSenderSessionLogs();
					
					if (eagerPurgeOpQueue) { purgeOpOutputQueue(); }
				}

				long tPurge = System.currentTimeMillis();
				logger.trace("Failure ctrl handling purged logs in "+(tPurge - tOpt)+" ms");

				//TODO: Get an appropriate value for this timeout.
				if (failureCtrlWatchdog != null) { failureCtrlWatchdog.reset(dsOpId, FAILURE_CTRL_WATCHDOG_TIMEOUT); }
				checkBatchRetransmitTimeouts(dsOpId);		
				logger.trace("Failure ctrl handling checked retransmits and reset watchdog in "+(System.currentTimeMillis() - tOpt)+" ms");
				lock.notifyAll();
				logger.debug("Handled failure ctrl in duration="+ (System.currentTimeMillis() - tStart));
			}
		}

		
		public void handleUpstreamFailureCtrl(FailureCtrl fctrl, int upOpId)
		{
			synchronized(lock)
			{
				//combinedDownFctrl.update(fctrl.lw(), fctrl.acks(), null);
				combinedDownFctrl.update(fctrl, false);
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
			//boolean removedSome = !opQueue.removeOlderInclusive(combinedDownFctrl.lw()).isEmpty();
			boolean removedSome = opQueue.removeOlderInclusive(combinedDownFctrl.lw());
			removedSome = removedSome || opQueue.removeAll(combinedDownFctrl.acks()).isEmpty();
			if (removedSome) { setDownstreamsRoutable(true); }
		}

		private void addBatchRetransmitTimer(int downOpId, long batchId, long now)
		{
			if (bestEffort || !enableBatchRetransmitTimeouts) { return; }

			//No point retransmitting if only one downstream replica!
			if (numDownstreamReplicas < 2) { return ; }

			synchronized(lock)
			{
				// Add (batchId, ts) to dsOpid.cache
				// Log/fail if attempted to retransmit via same for the moment as a reminder!
				if (batchRetransmitTimers.get(downOpId).containsKey(batchId)) 
				{ 
					logger.warn("Retransmission of ts="+batchId+" routed to same downstream="+downOpId);
				}
				batchRetransmitTimers.get(downOpId).put(batchId, now);
				logger.debug("Added retransmit timer for ts="+batchId+" to "+downOpId);
			}
		}

		private void clearBatchRetransmitTimers(int downOpId)
		{
			if (bestEffort) { throw new RuntimeException("TODO"); }
			
			synchronized(lock)
			{
				// Basically clear cache of downOpId.
				batchRetransmitTimers.get(downOpId).clear();
				logger.debug("Cleared retransmit timers for "+downOpId);
			}
		}

		private void checkBatchRetransmitTimeouts(int downOpId)
		{
			if (bestEffort) { throw new RuntimeException("TODO"); }
			if (!enableBatchRetransmitTimeouts) { return; }
			int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
			SynchronousCommunicationChannel cci = (SynchronousCommunicationChannel)owner.getPUContext().getDownstreamTypeConnection().elementAt(downOpIndex);
			IBuffer buffer = (OutOfOrderBuffer)cci.getBuffer();

			DataTuple testRoutesTuple = null;
			synchronized(lock)
			{

				logger.debug("Retransmit timers before check="+batchRetransmitTimers);
				Iterator<Map.Entry<Long,Long>> iter = batchRetransmitTimers.get(downOpId).entrySet().iterator();
				while (iter.hasNext())
				{
					Map.Entry<Long,Long> e = iter.next();
					Long batchId = e.getKey();
					long delay = System.currentTimeMillis() - e.getValue();
					if (combinedDownFctrl.isAcked(batchId) ||
						(optimizeReplay && combinedDownFctrl.isAlive(batchId)))
					{
						// Acked so remove batch from all caches.
						// TODO: Trickier if alive since if we delete and alive gets retracted due to failure ctrl timeout the batch won't have a retransmit timer? 
						// For now just ignore and delete in both cases.
						logger.debug("Removing retransmit timer for ts="+batchId+",dsOpId="+downOpId);
						iter.remove();
						for (Integer otherDownOpId : batchRetransmitTimers.keySet())
						{
							if (otherDownOpId != downOpId) { batchRetransmitTimers.get(otherDownOpId).remove(batchId); }	
						}  
					

					} 
					else if (delay > FAILURE_CTRL_RETRANSMIT_TIMEOUT)
					{

						// TODO: Could improve this with more info to routing about where to retransmit to.
						// TODO: Could be smarter about when its not worth retransmitting (e.g. if already sent to all downstreams)
						// TODO: Have a separate 'sent' data structure per ds to filter retransmit attempts
						BatchTuplePayload btp = buffer.get(batchId);

						if (btp != null)
						{
							logger.debug("Removing retransmit timer for ts="+batchId);
							iter.remove();
							for (Integer otherDownOpId : batchRetransmitTimers.keySet())
							{
								if (otherDownOpId != downOpId) { batchRetransmitTimers.get(otherDownOpId).remove(batchId); }	
							}  

							DataTuple dt = new DataTuple(idxMapper, btp.getTuple(0));
							opQueue.forceAdd(dt);
							testRoutesTuple = dt;
							logger.info("Retransmit timeout for ts="+batchId+" to "+downOpId+", delay="+delay+", readded to op queue.");
						}
						else
						{
							logger.warn("Ignoring retransmit timeout for ts="+batchId+" to "+downOpId+", delay="+delay+", no batch in buffer.");
						}
					}
					else
					{
						logger.debug("Retransmit timeout for ts="+batchId+" to "+downOpId+" not exceeded.");
					}
				}

				logger.debug("Retransmit timers after check="+batchRetransmitTimers);
			}
			if (testRoutesTuple != null) { checkAlternates(testRoutesTuple, downOpId); }
		}
	}
	
	private void checkAlternates(DataTuple dt, int downOpId)
	{
		int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
		ArrayList<Integer> targets = owner.getOperator().getRouter().forward_highestWeight(dt);
		int numTargets = targets == null ? 0 : targets.size();
		boolean containsDown = targets == null ? false : targets.contains(downOpIndex);
		logger.info("Watchdog requeueing some tuples for "+downOpId+" with "+numTargets+" targets, containsDown="+containsDown);
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
					logger.warn("Failure ctrl watchdog of "+owner.getOperator().getOperatorId() + " for "+downOpId+" expired.");
					DataTuple testRoutesTuple = handleFailureCtrlWatchdogTimeout(downOpId);
					if (testRoutesTuple != null) { checkAlternates(testRoutesTuple, downOpId); }
				} 
			};
			
			synchronized(lock) { currentTasks.put(downOpId, timeoutTask); }
			timer.schedule(timeoutTask, delay);
		}

		public DataTuple handleFailureCtrlWatchdogTimeout(int downOpId)
		{
			DataTuple testRoutesTuple = null;

			//TODO: Extend/chenage this to also temporarily disable failure ctrl updates for this downstream 
			//owner.getOperator().getRouter().update_downFailed(downOpId);
			notifyRoutingFailed(downOpId);
			
			if (hardReplay)
			{
				testRoutesTuple = postFailureCtrlTimeoutCleanupHard(downOpId);

			}
			else
			{
				if (broadcast) { throw new RuntimeException("TODO"); }
				testRoutesTuple = postFailureCtrlTimeoutCleanupSoft(downOpId);
			} 
			//TODO: Add a call to reenable failure ctrl updates for this downstream

			return testRoutesTuple;
		}
		//private void requeueTuples(TreeMap<Long, BatchTuplePayload> sessionLog, Set<Long> dsOpOldAlives)
		//N.B. Assumes the lock is held
		public DataTuple postFailureCtrlTimeoutCleanupSoft(int downOpId)
		{
			logger.info("Failure ctrl watchdog performing soft cleanup for "+downOpId);
			DataTuple testRoutesTuple = null;
			long tStart = System.currentTimeMillis();

			int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
			synchronized(lock)
			{
				clear(downOpId);
				if (fctrlHandler != null) { fctrlHandler.clearBatchRetransmitTimers(downOpId); }

				//TODO: What to do when a failure ctrl times out?
				//TODO: In theory this could fail to include the current
				//SEEP batch if it is multi-tuple and the dispatcher sends
				//tuples at a time - although it shouldn't really.

				//Should also clear fctrl?
				//Should get the current alives for this downstream
				//Or should it be the current combined alives?
				//Currently, this only replays tuples in the downstreams output buffer for which there is no alive from any downstream.
				//In particular, unlike when a connection fails it doesn't clear the alives of the downstream, so anything that is only at the downstream won't be 
				//replayed. Should probably be more aggressive and just treat it like a connection failure in terms of what gets replayed.
				//However, would also need to add code to replay anything in the shared replay log that's no longer alive elsewhere or been
				//transmitted already to a different downstream.
				//throw new RuntimeException("TODO: This doesn't clear downOpId.downAlives, so won't replay tuples alive only at that downstream.");
				
				FailureCtrl downOpFailureCtrl = getCombinedDownFailureCtrl();
				SynchronousCommunicationChannel cci = (SynchronousCommunicationChannel)owner.getPUContext().getDownstreamTypeConnection().elementAt(downOpIndex);
				if (cci != null)
				{
					IBuffer buffer = cci.getBuffer();
					TreeMap<Long, BatchTuplePayload> delayedBatches = buffer.get(downOpFailureCtrl);
					if (broadcast) { throw new RuntimeException("TODO: call to buffer.get must return alive tuples unless alive at *all* downstreams?"); }
					logger.debug("Watchdog requeueing "+delayedBatches.size()+" tuples sent to "+downOpId);
					long now = System.currentTimeMillis();
					for (Map.Entry<Long, BatchTuplePayload> e : delayedBatches.entrySet())
					{
						long ts = e.getKey();
						TuplePayload p = e.getValue().getTuple(0);	//TODO: Proper batches.

						if (!combinedDownFctrl.isAcked(ts))
						{	
							//TODO: what if acked already?
							DataTuple dt = new DataTuple(idxMapper, p);
							long latency = now - dt.getPayload().instrumentation_ts;
							if (!(optimizeReplay && combinedDownFctrl.isAlive(ts)))
							{
								logger.debug("Watchdog requeueing data tuple sent to "+downOpId+" with ts="+p.timestamp+",latency="+latency);
								opQueue.forceAdd(dt);
								testRoutesTuple = dt;
							}
						}
					}
					//TODO: Should clear routing info for op perhaps?
					//Readd to output queue (N.B. While 'get' is actually 'trim' temporarily, must be careful not
					//to lose tuples here!)
					//TODO: Can perhaps reuse requeueTuples code here.
					
				}
				lock.notifyAll();
			} 

			logger.info("Failure ctrl watchdog completed soft cleanup for "+ downOpId +" in "+(System.currentTimeMillis() - tStart) +" ms");

			return testRoutesTuple;
		}

		private DataTuple postFailureCtrlTimeoutCleanupHard(int downOpId)
		{
			//TODO: Need to make sure no more are added to buffer subsequently?
			//TODO: Is it possible that tuples are getting added and then never retransmitted at the moment? e.g. if
			//we have a failure ctrl or retransmit timeout, and happen to be transmitting a new tuple, then if we clear
			//its retransmit timer it might not get readded?

			logger.warn("Failure ctrl watchdog performing hard cleanup for "+downOpId);
			long tStart = System.currentTimeMillis();
			int downOpIndex = owner.getOperator().getOpContext().getDownOpIndexFromOpId(downOpId);
			SynchronousCommunicationChannel dest = (SynchronousCommunicationChannel)owner.getPUContext().getDownstreamTypeConnection().elementAt(downOpIndex);
			boolean assumeFailOnHardReplay = true;
			if (dest != null)
			{
				synchronized(lock)
				{
					clear(downOpId);
					if (fctrlHandler != null) { fctrlHandler.clearBatchRetransmitTimers(downOpId); }
					
					//holding the lock
					//1) compute the new joint alives
					//2) Do a combined.set alives
					//3) Save the old alives for this downstream
					//4) delete the old alives for this downstream
					Set<Long> dsOpOldAlives = updateDownAlives(downOpId, null);

					//TODO: Should we be using downOpFailureCtrl?
					FailureCtrl downOpFailureCtrl = getCombinedDownFailureCtrl();
					logger.debug("node fctrl after updateDownAlives: "+downOpFailureCtrl+",dsooa: "+RangeUtil.toRangeSetStr(dsOpOldAlives));
					IBuffer buffer = dest.getBuffer();
					//N.B. This is the key step wrt mhro!

					TreeMap<Long, BatchTuplePayload> delayedBatches = assumeFailOnHardReplay? buffer.trim(null) : buffer.get(downOpFailureCtrl);
					if (!assumeFailOnHardReplay && broadcast) { throw new RuntimeException("TODO: call to buffer.get must return alive tuples unless alive at *all* downstreams?"); }
					
					//5) for tuple in logged
					//		if acked discard
					//		else if in joint alives add to shared replay log
					//		else add to output queue 
					//
					//		remove from the alives for the old fctrl for this downstream
					requeueTuples(delayedBatches, dsOpOldAlives);

					// Check whether there have been any retractions for batches only contained in other buffers.
					Set<Long> retractions = getRetractions(dsOpOldAlives);
					if (!retractions.isEmpty()) 
					{ 
						logger.info("Downstream "+downOpId+" retractions:"+retractions); 
						//requeueRetractedTuples(dsOpId, retractions);
						requeueRetractedTuples(retractions);
					}

					//6) For remaining tuples in old fctrl for this downstream
					//		if tuple not acked and not in new joint alives and tuple in shared replay log
					//			move tuple from shared replay log to output queue
					//TODO: Should this be outside this if block?
					logger.info("Dispatcher worker "+downOpId+" checking for replay from shared log after hard timeout.");
					logger.debug("dsooa before requeue from srl: "+RangeUtil.toRangeSetStr(dsOpOldAlives));
					requeueFromSharedReplayLog(dsOpOldAlives);
					logger.error("TODO: Should we be requeueing from srl before from individual buffers (correctness vs perf?)");

					if (assumeFailOnHardReplay) { workers.get(downOpIndex).notifyFctrlHardTimedOut(); }
					lock.notifyAll();
				}
			}	

			logger.warn("Failure ctrl watchdog completed hard cleanup for "+dest.getOperatorId()+" in "+(System.currentTimeMillis() - tStart) +" ms");
			//Update this connections routing cost 
			//TODO: Should you be doing this route cost update before/after/synchronously with the replay?
			//TODO: Need a method on router like setFailed (and then unsetFailed).
			//TODO: Need to ignore failure ctrl's in dispatcher too until after we've reenabled their handling in the router.
			// Or maybe we can filter them inside the dispatcher first?
			//owner.getOperator().getRouter().update_downFailed(dest.getOperatorId());
			//notifyRoutingFailed(dest.getOperatorId());
			//TODO: Should probably convert this to void.	
			return null;
		}

		public void clear(int downOpId)
		{
			synchronized(lock)
			{
				if (currentTasks.containsKey(downOpId))
				{
					currentTasks.remove(downOpId).cancel();
				}
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
				try
				{
					queue.put(dt.getPayload().timestamp, dt);
				}
				catch(RuntimeException e)
				{
					logger.error("Cannot put timestamp="+dt.getPayload().timestamp+", map="+queue);
					throw e;
				}
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
					Iterator<Map.Entry<Long, DataTuple>> iter = queue.entrySet().iterator();
					while (iter.hasNext())
					{
						Map.Entry<Long,DataTuple> qEntry = iter.next();
						Long qts = qEntry.getKey();
						if (tsSet.contains(qts))
						{
							iter.remove();
							DataTuple dt = qEntry.getValue();
							if (dt != null) { removed.put(qts,  dt); }
						}
					}
					/*
					for (Long qts : queue.keySet())
					{
						if (tsSet.contains(qts))
						{
							DataTuple dt = queue.remove(qts);
							if (dt != null) { removed.put(qts,  dt); }
						}
					}
					*/
				}
				if (!removed.isEmpty()) { lock.notifyAll(); }
			}
			return removed;
		}
		
		//public SortedMap<Long, DataTuple> removeOlderInclusive(long ts)
		public boolean removeOlderInclusive(long ts)
		{
			synchronized(lock)
			{
				//SortedMap<Long, DataTuple> removed = new TreeMap<>(queue.headMap(ts+1));
				SortedMap<Long, DataTuple> removed = queue.headMap(ts+1);
				boolean removedSome = removed != null && !removed.isEmpty();
				SortedMap<Long, DataTuple> remainder = queue.tailMap(ts+1);
				if (remainder == null || remainder.isEmpty()) { queue.clear(); }
				else 
				{ 
					//queue = remainder; Don't do this since queue will have a restricted range
					//Not necessarily what we want for sharedReplayLog?
					removed.clear(); //Should be reflected in the backing map.
				}
				if (remainder != null && remainder.size() != queue.size()) 
				{ throw new RuntimeException("Logic error: rsz="+remainder.size()+", qsz="+queue.size()+" - non-backing map?"); }

				//if (removed != null && !removed.isEmpty()) { lock.notifyAll(); }
				if (removedSome) { lock.notifyAll(); }
				return removedSome;
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
