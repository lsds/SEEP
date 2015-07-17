package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class BackpressureRouter implements IRouter {

	private final static Logger logger = LoggerFactory.getLogger(BackpressureRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final boolean downIsMultiInput;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Long> lastWeightUpdateTimes;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private final WeightExpiryMonitor weightExpiryMonitor = new WeightExpiryMonitor();

	
	public BackpressureRouter(OperatorContext opContext) {
		this.weights = new HashMap<>();
		this.lastWeightUpdateTimes = new HashMap<>();
		this.unmatched = new HashMap<>();
		this.opContext = opContext;
		ArrayList<Integer> downOps = opContext.getDownstreamOpIdList();
		for (int downOpId : downOps)
		{
			weights.put(downOpId, INITIAL_WEIGHT);
			lastWeightUpdateTimes.put(downOpId, -1L);
			unmatched.put(downOpId, new HashSet<Long>());
		}
		logger.info("Initial weights: "+weights);
		Query meanderQuery = opContext.getMeanderQuery(); 
		int logicalId = meanderQuery.getLogicalNodeId(opContext.getOperatorStaticInformation().getOpId());
		int downLogicalId = meanderQuery.getNextHopLogicalNodeId(logicalId); 

		downIsMultiInput = meanderQuery.getLogicalInputs(downLogicalId).length > 1;
	}
	
	public ArrayList<Integer> route(long batchId)
	{
		ArrayList<Integer> targets = null;
		synchronized(lock)
		{
			if (downIsMultiInput)
			{
				for (Integer downOpId : unmatched.keySet())
				{
					if (unmatched.get(downOpId).contains(batchId))
					{
						if (targets == null)
						{
							targets = new ArrayList<>();
							targets.add(-1);	//Hack: allows dispatcher to distinguish constrained routing.
							logger.info("Adding routing constraints for "+batchId);
						}
						//Don't care about weight, must at least be connected if non-empty constraints.
						//Problem: If catchup weight has changed!
						//TODO: Can perhaps optimize this, although need to be careful.
						targets.add(opContext.getDownOpIndexFromOpId(downOpId));
						logger.info("Added routing constraint to "+downOpId);
					}
				}
			}
			
			//If no constrained routes, try to get based on weight.
			if (targets == null)
			{
				Integer downOpId = maxWeightOpId();
				
				if (downOpId != lastRouted)
				{
					switchCount++;
					logger.info("Switched route from "+lastRouted + " to "+downOpId+" (switch cnt="+switchCount+")");
					lastRouted = downOpId;
				}
				if (downOpId != null)
				{
					targets = new ArrayList<>();
					targets.add(opContext.getDownOpIndexFromOpId(downOpId));
				}
				else { return null; }
			}
		}
		return targets;
	}
	
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp)
	{
		return handleDownUp(downUp, true);
	}

	private Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp, boolean resetExpiryTimer)
	{
		Map<Integer, Set<Long>> newConstraints = null;
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId()))
			{
				throw new RuntimeException("Logic error?");
			}
			logger.debug("BP router handling downup rctrl: "+ downUp);
			long prevUpdateTs = lastWeightUpdateTimes.get(downUp.getOpId());
			lastWeightUpdateTimes.put(downUp.getOpId(), System.currentTimeMillis());
			if (resetExpiryTimer)
			{
				weightExpiryMonitor.reset(downUp.getOpId());
			}
			
			if (prevUpdateTs > 0) 
			{ 
				logger.info("Weight update delay for "+downUp.getOpId()+"="+(System.currentTimeMillis() - prevUpdateTs));
			}
			
			weights.put(downUp.getOpId(), downUp.getWeight());
			
			if (!resetExpiryTimer)
			{
				Integer newMaxWeightOpId = maxWeightOpId();
				if (maxWeightOpId() != null)
				{
					logger.info("Downstream timed out with alternative max weight downstream="+newMaxWeightOpId);
				}
			}
			
			logger.debug("Backpressure router weights= "+weights);
			Set<Long> newUnmatched = downUp.getUnmatched();
			if (newUnmatched != null)
			{
				//TODO: Tmp hack: Null here indicates a local update because
				//an attempt to send a q length msg upstream failed - should
				//clean it up to perhaps use a different method.
				Set<Long> oldUnmatched = unmatched.get(downUp.getOpId());
				boolean changed = unmatchedChanged(oldUnmatched, newUnmatched);
				unmatched.put(downUp.getOpId(), newUnmatched);
				
				if (changed) {
					newConstraints = new HashMap<Integer, Set<Long>>();
					newConstraints.put(downUp.getOpId(), newUnmatched);
				}
			}
			else
			{
				unmatched.get(downUp.getOpId()).clear();
			}
		}
		return newConstraints;
	}
	
	private Integer maxWeightOpId()
	{
		Integer result = null;
		double maxWeight = 0;
		synchronized(lock)
		{
			for (Integer opId : weights.keySet())
			{
				double opWeight = weights.get(opId);
				if (opWeight > maxWeight) { result = opId; maxWeight = opWeight; }
			}
			logger.debug("maxWeight: Backpressure router weights= "+weights);
		}
		return result;
	}

	@Override
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState) {
		throw new RuntimeException("Logic error");		
	}
	
	public Set<Long> areConstrained(Set<Long> queued)
	{
		if (queued == null || queued.isEmpty() || !downIsMultiInput) { return null; }
		logger.debug("Checking constrained for queued: "+queued);
		synchronized(lock)
		{
			Set<Long> constraints = new HashSet<>();
			for (Integer dsOpId : unmatched.keySet())
			{
				for (Long constrained : unmatched.get(dsOpId))
				{
					if (queued.contains(constrained))
					{
						constraints.add(constrained);
					}
				}
			}
			logger.debug("Constrained in queue: "+constraints);
			return constraints;
		}
	}
	
	private boolean unmatchedChanged(Set<Long> oldUnmatched, Set<Long> newUnmatched)
	{
		if (oldUnmatched == null && newUnmatched == null) { return true;}		
		if (oldUnmatched == null || newUnmatched == null) { return false;}
		if (oldUnmatched.size() != newUnmatched.size()) { return false; }
		
		for (Long unmatched : oldUnmatched) 
		{ 
			if (!newUnmatched.contains(unmatched)) { return false; }
		}
		return true;
	}
	
	private class WeightExpiryMonitor
	{
		final static long DOWNSTREAM_MAX_WEIGHT_DELAY = 2 * 1000;
		final static long WEIGHT_TIMEOUT = 2 * DOWNSTREAM_MAX_WEIGHT_DELAY;
		final Timer timer = new Timer(true);
		final Map<Integer, TimerTask> currentTasks = new HashMap<>();
		public void reset(final int opId)
		{
			if (currentTasks.containsKey(opId))
			{
				currentTasks.remove(opId).cancel();
			}
			TimerTask timeoutTask = new TimerTask() 
			{ 
				public void run() 
				{  
					//TODO: Bit wary about causing some kind of deadlock here.
					//Also, never actually notify the dispatcher about the change.
					handleDownUp(new DownUpRCtrl(opId, -1.0, null));
				} 
			};
			
			currentTasks.put(opId, timeoutTask);
			timer.schedule(timeoutTask, WEIGHT_TIMEOUT);
		}
	}
}
