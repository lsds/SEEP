package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;
import uk.ac.imperial.lsds.seep.manet.Query;

public class OutOfOrderBufferedBarrier implements DataStructureI {

	private final static Logger logger = LoggerFactory.getLogger(OutOfOrderBufferedBarrier.class);
	private final int opId;
	private final int logicalId;
	private final Query meanderQuery;
	private final int numLogicalInputs;
	private final ArrayList<TreeMap<Timestamp, DataTuple>> pending;	//Unbounded
	private final TreeMap<Timestamp, ArrayList<DataTuple>> ready = new TreeMap<Timestamp, ArrayList<DataTuple>>();
	private final ArrayList<FailureCtrl> inputFctrls;
	private final boolean optimizeReplay;
	private final boolean bestEffort;
	private final boolean reprocessNonLocals;
	private final int maxReadyQueueSize;
	private final boolean boundReadyQueue;
	private final long barrierTimeout;
	private final BarrierTimeoutMonitor barrierTimeoutMonitor;
	
	public OutOfOrderBufferedBarrier(Query meanderQuery, int opId)
	{
		this.opId = opId;
		this.meanderQuery = meanderQuery;	//To get logical index for upstreams.
		this.logicalId = meanderQuery.getLogicalNodeId(opId);
		this.numLogicalInputs = meanderQuery.getLogicalInputs(meanderQuery.getLogicalNodeId(opId)).length;
		this.bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		this.optimizeReplay = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay"));
		this.reprocessNonLocals = Boolean.parseBoolean(GLOBALS.valueFor("reprocessNonLocals"));
		//this.maxReadyQueueSize = Integer.parseInt(GLOBALS.valueFor("inputQueueLength"));
		this.maxReadyQueueSize = Integer.parseInt(GLOBALS.valueFor("readyQueueLength"));
		this.barrierTimeout = Long.parseLong(GLOBALS.valueFor("barrierTimeout"));
		this.boundReadyQueue = Boolean.parseBoolean(GLOBALS.valueFor("boundReadyQueue")) || 
					!GLOBALS.valueFor("meanderRouting").equals("backpressure") ||  
					meanderQuery.getPhysicalNodeIds(logicalId).size() == 1;
		logger.info("OutOfOrderBufferedBarrier using bound ready queue? "+this.boundReadyQueue);

		if (barrierTimeout > 0)  
		{ 
			logger.info("Setting up barrier timeout monitor with delay="+barrierTimeout);
			barrierTimeoutMonitor = new BarrierTimeoutMonitor(); 
		} 
		else { barrierTimeoutMonitor = null; } 
		
		if (numLogicalInputs != 2) { throw new RuntimeException("TODO"); }
		inputFctrls = new ArrayList<>(numLogicalInputs);	//TODO: Bit redundant to have per input fctrls?
		pending = new ArrayList<>(numLogicalInputs);
		for (int i = 0; i < numLogicalInputs; i++)
		{
			pending.add(new TreeMap<Timestamp, DataTuple>());
			inputFctrls.add(new FailureCtrl());
		}
	}
	
	//TODO: Note the incoming data handler worker
	//could tell us both the upOpId, the upOpOriginalId
	//or even the meander query index.
	public void push(DataTuple dt, int upOpId)
	{
		synchronized(this)
		{
			Timestamp ts = dt.getPayload().timestamp;
			int logicalInputIndex = meanderQuery.getLogicalInputIndex(logicalId, meanderQuery.getLogicalNodeId(upOpId));
			FailureCtrl inputFctrl = inputFctrls.get(logicalInputIndex); 
			if (pending.get(logicalInputIndex).containsKey(ts) || inputFctrl.isAcked(ts) || inputFctrl.isAlive(ts))
			{
				logger.debug("Ignoring tuple with ts="+ts);
				return; 
			}
			
			boolean tsReady = true;
			pending.get(logicalInputIndex).put(ts, dt);
			for (int i = 0; i < numLogicalInputs; i++)
			{
				if (i == logicalInputIndex) { continue; }
				if (!pending.get(i).containsKey(ts))
				{
					tsReady = false;
					logger.debug("Adding "+ts+" to pending queue "+logicalInputIndex);
					//pending.get(logicalInputIndex).put(ts, dt);
					break;
				}
			}
			
			if (tsReady)
			{
				logger.debug("Tuple "+ts+" all ready."); 
				addReady(ts);
				if (barrierTimeoutMonitor != null) { barrierTimeoutMonitor.clear(ts); }
				
				while (boundReadyQueue && ready.size() > maxReadyQueueSize)
				{
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}
			else if (barrierTimeoutMonitor != null)
			{
				//TODO: Should perhaps timeout earlier depending on distance to sink? 
				//long delay = dt.getPayload().instrumentation_ts + barrierTimeout - System.currentTimeMillis();
				long delay = barrierTimeout;
				delay = delay > 0 ? delay : 1;
				barrierTimeoutMonitor.set(ts, delay);
			}
		}
	}
	
	//Assumes lock held
	private void addReady(Timestamp ts)
	{
		ArrayList<DataTuple> readyBatches = new ArrayList<>(numLogicalInputs);
		for (int i = 0; i < numLogicalInputs; i++)
		{
			if (!bestEffort) { inputFctrls.get(i).updateAlives(ts); }	//TODO: Should just have 1?
			readyBatches.add(pending.get(i).remove(ts));
			/*
			if (i == logicalInputIndex) { readyBatches.add(dt); }
			else { readyBatches.add(pending.get(i).remove(ts)); }
			*/
		}
		
		long readyTime = System.currentTimeMillis();
		String msg  = "Pending latencies for ts="+ts+":";
		for (int i = 0; i < readyBatches.size(); i++)
		{
			if (readyBatches.get(i) == null) { continue; }
			long latency = readyTime - readyBatches.get(i).getPayload().instrumentation_ts;
			long pendingLatency = readyTime - readyBatches.get(i).getPayload().local_ts;
			readyBatches.get(i).getPayload().local_ts = readyTime;
			msg += "idx="+i+";latency="+latency+";pending="+pendingLatency;
			if (i < readyBatches.size() - 1) { msg += ","; }
		}
		logger.debug(msg);
		
		//TODO: Clear any timers for this ts
		
		ready.put(ts, readyBatches);
		this.notifyAll();
	}
	
	@Override
	public synchronized ArrayList<DataTuple> pull_from_barrier() {
		long pullStart = System.currentTimeMillis();
		while(ready.isEmpty())
		{
			logger.debug("Waiting for ready batches.");
			try {
				this.wait();
			} catch (InterruptedException e) {
				logger.warn("Unexpectedly interrupted while waiting on barrier.");
			}
		}
		logger.debug("Pulling batches with ts="+ready.firstKey());
		ArrayList<DataTuple> dts = ready.remove(ready.firstKey());
		
		long pullEnd = System.currentTimeMillis();
		for (int i = 0; i < dts.size(); i++)
		{
			if (dts.get(i) == null) { continue; }
			Timestamp ts = dts.get(i).getPayload().timestamp;
			long latency = pullEnd - dts.get(i).getPayload().instrumentation_ts;
			long pullLatency = pullEnd - dts.get(i).getPayload().local_ts;
			long pullReadTime = pullEnd - pullStart;
			
			logger.debug("Pulled tuple with ts="+ts+",latency="+latency+",pullLatency="+pullLatency+",pullReadTime="+pullReadTime);
		}
		for (DataTuple dt : dts) 
		{ 
			if (dt != null) { dt.getPayload().local_ts = pullEnd; }
		}	
		this.notifyAll();
		return dts;
	}

	@Override
	public synchronized ArrayList<FailureCtrl> purge(FailureCtrl downFctrl) {
		if (bestEffort) { throw new RuntimeException("Logic error"); }
		
		for (int i = 0; i < numLogicalInputs; i++)
		{
			inputFctrls.get(i).update(downFctrl, true, true);
		}
		
		//Now purge the ready queue of any acked batches.
		trimQueue(ready.keySet().iterator(), downFctrl);

		//Now purge each input's pending queue.
		for (int i = 0; i < numLogicalInputs; i++)
		{
			trimQueue(pending.get(i).keySet().iterator(), downFctrl);
		}
		
		ArrayList<FailureCtrl> upOpFctrls = new ArrayList<>(numLogicalInputs);
		if (optimizeReplay)
		{
			for (int i = 0; i < numLogicalInputs; i++)
			{
				FailureCtrl upOpFctrl = new FailureCtrl(inputFctrls.get(i));
				upOpFctrl.updateAlives(downFctrl.alives());
				upOpFctrls.add(upOpFctrl);
			}
		}
		else
		{
			for (int i = 0; i < numLogicalInputs; i++)
			{
				upOpFctrls.add(new FailureCtrl(downFctrl));
			}
		}
	
		this.notifyAll();
		
		return upOpFctrls; 
	}
	
	private void trimQueue(Iterator<Timestamp> qIter, FailureCtrl downFctrl)
	{
		while (qIter.hasNext())
		{
			Timestamp ts = qIter.next();
			//if (ts <= downFctrl.lw() || downFctrl.acks().contains(ts)
			if (downFctrl.isAcked(ts)
					//|| (!reprocessNonLocals && downFctrl.alives().contains(ts)))
					|| (!reprocessNonLocals && downFctrl.isAlive(ts)))
			{
				qIter.remove();
				if (barrierTimeoutMonitor != null) { barrierTimeoutMonitor.clear(ts); }
			}
		}
	}
	
	
	//Should return the 'total' queue length at index -1,
	//With the other queue lengths at logical input index 0,1 (if a join).
	//TODO: Not sure exactly what the best total size should be?
	//At the moment its the ready plus the sum of all the pendings.
	//Could alternatively go for ready plus the avg of all the pendings.
	public synchronized Map<Integer, Integer> sizes() {
		Map<Integer, Integer> sizes = new HashMap<>();
		sizes.put(-1, ready.size());
		for (int i = 0; i < numLogicalInputs; i++)
		{
			sizes.put(i, pending.get(i).size());
		}
		logger.debug("op "+opId+" sizes="+sizes);
		return sizes;
	}

	/*
	public synchronized Map<Integer, Integer> sizes() {
		Map<Integer, Integer> sizes = new HashMap<>();
		Map<Integer, Integer> pendingSizes = new HashMap<>();
		sizes.put(-1, ready.size());
		pendingSizes.put(-1,  ready.size());
		for (int i = 0; i < numLogicalInputs; i++)
		{
			sizes.put(i, ready.size() + pending.get(i).size());
			sizes.put(-1, sizes.get(-1) + pending.get(i).size());
			
			pendingSizes.put(i,  pending.get(i).size());
		}
		logger.debug("op "+opId+" sizes="+sizes+", pendingSizes="+pendingSizes+", ready="+ready.size());
		return sizes;
	}
	*/
	
	@Override
	public int size() {
		throw new RuntimeException("Logic error.");
	}

	/** Get the current 'constraints' i.e. for each input the set
	 * of batch ids not yet received but already received for other inputs 
	 */
	public synchronized ArrayList<RangeSet<Timestamp>> getRoutingConstraints() {
		ArrayList<TreeSet<Timestamp>> constraints = new ArrayList<>(numLogicalInputs);
		ArrayList<RangeSet<Timestamp>> constraintRanges = new ArrayList<>(numLogicalInputs);
		for (int i = 0; i < numLogicalInputs; i++)
		{
			constraints.add(new TreeSet<Timestamp>());
			
			for (int j = 0; j < numLogicalInputs; j++)
			{
				if (i == j) { continue; }
				// TODO: if numLogicalInputs > 2 should really have a count for each constraint
				constraints.get(i).addAll(pending.get(j).keySet());
			}
			constraintRanges.add(toRangeSet(constraints.get(i)));
		}
		logger.debug("Constraint ranges: "+constraintRanges+", constraints:"+constraints);
		return constraintRanges;
	}
	
	private RangeSet<Timestamp> toRangeSet(TreeSet<Timestamp> constraints)
	{ 
		/*
		RangeSet<Timestamp> result = TreeRangeSet.create();
		if (constraints == null || constraints.isEmpty()) { return result; }
		
		Iterator<Timestamp> iter = constraints.iterator();
		Timestamp rangeStart = null;
		Timestamp rangeEnd = null;
		while(iter.hasNext())
		{
			Timestamp next = iter.next();
			if (rangeStart == null)
			{
				rangeStart = next;
				rangeEnd = next;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd));
					break;
				}
			}
			else if (next == rangeEnd + 1)
			{
				rangeEnd++;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd));
					break;
				}
			}
			else
			{
				result.add(Range.closed(rangeStart, rangeEnd));
				rangeStart = next;
				rangeEnd = next;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd));
					break;
				}
			}
		}
		
		return result;
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");
	}
	
	@Override
	public void push(DataTuple dt) {
		throw new RuntimeException("Logic error - use push(DataTuple, int)");
	}

	@Override
	public DataTuple pull() {
		throw new RuntimeException("Logic error - use pull_from_barrier()");
	}

	private class BarrierTimeoutMonitor
	{
		private final Map<Timestamp, TimerTask> timeoutTasks = new HashMap<>();
		private final Timer timer = new Timer(true);
		
		public void set(final Timestamp ts, long delay)
		{
			if (timeoutTasks.containsKey(ts)) { throw new RuntimeException("Logic error - timeout task already exists: "+ts); }
			TimerTask timeoutTask = new TimerTask() 
			{ 
				public void run() 
				{  

					logger.warn("Nonblocking join "+ts+" timed out."); 
					synchronized(OutOfOrderBufferedBarrier.this)
					{					
						if (timeoutTasks.remove(ts) != null)
						{
							addReady(ts);
						}
					}
					//TODO:
				} 
			};
			
			timeoutTasks.put(ts, timeoutTask);
			timer.schedule(timeoutTask, delay);
		}
		
		public void clear(final Timestamp ts)
		{
			if (timeoutTasks.containsKey(ts))
			{
				timeoutTasks.remove(ts).cancel();
			}			
		}
	}
}
