package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.manet.Query;

public class OutOfOrderBufferedBarrier implements DataStructureI {

	private final static Logger logger = LoggerFactory.getLogger(OutOfOrderBufferedBarrier.class);
	private final int logicalId;
	private final Query meanderQuery;
	private final int numLogicalInputs;
	private final ArrayList<TreeMap<Long, DataTuple>> pending;	//Unbounded
	private final TreeMap<Long, ArrayList<DataTuple>> ready = new TreeMap<Long, ArrayList<DataTuple>>(); //Unbounded
	private final ArrayList<FailureCtrl> inputFctrls;
	private final boolean optimizeReplay;
	private final boolean bestEffort;
	private final boolean reprocessNonLocals;
	
	public OutOfOrderBufferedBarrier(Query meanderQuery, int opId)
	{
		this.meanderQuery = meanderQuery;	//To get logical index for upstreams.
		this.logicalId = meanderQuery.getLogicalNodeId(opId);
		this.numLogicalInputs = meanderQuery.getLogicalInputs(meanderQuery.getLogicalNodeId(opId)).length;
		this.bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		this.optimizeReplay = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay"));
		this.reprocessNonLocals = Boolean.parseBoolean(GLOBALS.valueFor("reprocessNonLocals"));
		
		if (numLogicalInputs != 2) { throw new RuntimeException("TODO"); }
		inputFctrls = new ArrayList<>(numLogicalInputs);	//TODO: Bit redundant to have per input fctrls?
		pending = new ArrayList<>(numLogicalInputs);
		for (int i = 0; i < numLogicalInputs; i++)
		{
			pending.add(new TreeMap<Long, DataTuple>());
			inputFctrls.add(new FailureCtrl());
		}
	}
	
	//TODO: Note the incoming data handler worker
	//could tell us both the upOpId, the upOpOriginalId
	//or even the meander query index.
	public synchronized void push(DataTuple dt, int upOpId)
	{
		long ts = dt.getPayload().timestamp;
		int logicalInputIndex = meanderQuery.getLogicalInputIndex(logicalId, meanderQuery.getLogicalNodeId(upOpId));
		FailureCtrl inputFctrl = inputFctrls.get(logicalInputIndex); 
		if (pending.get(logicalInputIndex).containsKey(ts) || inputFctrl.isAcked(ts) || inputFctrl.alives().contains(ts))
		{
			logger.debug("Ignoring tuple with ts="+ts);
			return; 
		}
		
		boolean tsReady = true;
		for (int i = 0; i < numLogicalInputs; i++)
		{
			if (i == logicalInputIndex) { continue; }
			if (!pending.get(i).containsKey(ts))
			{
				tsReady = false;
				logger.debug("Adding "+ts+" to pending queue "+logicalInputIndex);
				pending.get(logicalInputIndex).put(ts, dt);
				break;
			}
		}
		
		if (tsReady)
		{
			logger.debug("Tuple "+ts+" ready."); 
			ArrayList<DataTuple> readyBatches = new ArrayList<>(numLogicalInputs);
			for (int i = 0; i < numLogicalInputs; i++)
			{
				inputFctrls.get(i).updateAlives(ts);	//TODO: Should just have 1?
				if (i == logicalInputIndex) { readyBatches.add(dt); }
				else { readyBatches.add(pending.get(i).remove(ts)); }
			}
			
			long readyTime = System.currentTimeMillis();
			String msg  = "Pending latencies for ts="+ts+":";
			for (int i = 0; i < readyBatches.size(); i++)
			{
				long latency = readyTime - readyBatches.get(i).getPayload().instrumentation_ts;
				long pendingLatency = readyTime - readyBatches.get(i).getPayload().local_ts;
				readyBatches.get(i).getPayload().local_ts = readyTime;
				msg += "idx="+i+";latency="+latency+";pending="+pendingLatency;
				if (i < readyBatches.size() - 1) { msg += ","; }
			}
			logger.info(msg);
			ready.put(ts, readyBatches);
			this.notifyAll();
		}
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
		long ts = dts.get(0).getPayload().timestamp;
		long latency = pullEnd - dts.get(0).getPayload().instrumentation_ts;
		long pullLatency = pullEnd - dts.get(0).getPayload().local_ts;
		long pullReadTime = pullEnd - pullStart;
		
		logger.info("Pulled tuple with ts="+ts+",latency="+latency+",pullLatency="+pullLatency+",pullReadTime="+pullReadTime);
		for (DataTuple dt : dts) { dt.getPayload().local_ts = pullEnd; }
		this.notifyAll();
		return dts;
	}

	@Override
	public synchronized ArrayList<FailureCtrl> purge(FailureCtrl downFctrl) {
		if (bestEffort) { throw new RuntimeException("Logic error"); }
		
		for (int i = 0; i < numLogicalInputs; i++)
		{
			inputFctrls.get(i).update(downFctrl.lw(), downFctrl.acks(), null);
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
	
	private void trimQueue(Iterator<Long> qIter, FailureCtrl downFctrl)
	{
		while (qIter.hasNext())
		{
			Long ts = qIter.next();
			if (ts <= downFctrl.lw() || downFctrl.acks().contains(ts)
					|| (!reprocessNonLocals && downFctrl.alives().contains(ts)))
			{
				qIter.remove();
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
			sizes.put(i, ready.size() + pending.get(i).size());
			sizes.put(-1, sizes.get(-1) + pending.get(i).size());
		}
		logger.debug("Sizes="+sizes);
		return sizes;
	}
	
	@Override
	public int size() {
		throw new RuntimeException("Logic error.");
	}

	/** Get the current 'constraints' i.e. for each input the set
	 * of batch ids not yet received but already received for other inputs 
	 */
	public synchronized ArrayList<RangeSet<Long>> getRoutingConstraints() {
		ArrayList<TreeSet<Long>> constraints = new ArrayList<>(numLogicalInputs);
		ArrayList<RangeSet<Long>> constraintRanges = new ArrayList<>(numLogicalInputs);
		for (int i = 0; i < numLogicalInputs; i++)
		{
			constraints.add(new TreeSet<Long>());
			
			for (int j = 0; j < numLogicalInputs; j++)
			{
				if (i == j) { continue; }
				// TODO: if numLogicalInputs > 2 should really have a count for each constraint
				constraints.get(i).addAll(pending.get(j).keySet());
			}
			constraintRanges.add(toRangeSet(constraints.get(i)));
		}
		logger.debug("Constraints: "+constraintRanges);
		return constraintRanges;
	}
	
	private RangeSet<Long> toRangeSet(TreeSet<Long> constraints)
	{ 
		RangeSet<Long> result = TreeRangeSet.create();
		if (constraints == null || constraints.isEmpty()) { return result; }
		
		Iterator<Long> iter = constraints.iterator();
		Long rangeStart = null;
		Long rangeEnd = null;
		while(iter.hasNext())
		{
			Long next = iter.next();
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
				rangeStart = null;
				rangeEnd = null;
			}
		}
		return result;
	}
	
	@Override
	public void push(DataTuple dt) {
		throw new RuntimeException("Logic error - use push(DataTuple, int)");
	}

	@Override
	public DataTuple pull() {
		throw new RuntimeException("Logic error - use pull_from_barrier()");
	}

}
