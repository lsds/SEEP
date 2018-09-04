package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.GLOBALS;

/**
 * Backpressure Weighted Round Robin supports only should support both upstream and downstream 
 * routing control, but only downstream controllers have been tested so far.
 */
public class BackpressureWeightedRoundRobinRouter implements IRouter {
	private final static Logger logger = LoggerFactory.getLogger(BackpressureWeightedRoundRobinRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private final Random random = new Random(0);
	private final boolean upstreamRoutingController;
	private final boolean downIsMultiInput;
	private final boolean downIsUnreplicatedSink;
	
	
	public BackpressureWeightedRoundRobinRouter(OperatorContext opContext) {
		this.weights = new HashMap<>();
		this.unmatched = new HashMap<>();
		this.opContext = opContext;
		ArrayList<Integer> downOps = opContext.getDownstreamOpIdList();
		for (int downOpId : downOps)
		{
			weights.put(downOpId, INITIAL_WEIGHT);
			unmatched.put(downOpId, new HashSet<Long>());
		}
		logger.info("Initial weights: "+weights);
		Query frontierQuery = opContext.getFrontierQuery(); 
		int logicalId = frontierQuery.getLogicalNodeId(opContext.getOperatorStaticInformation().getOpId());
		int downLogicalId = frontierQuery.getNextHopLogicalNodeId(logicalId); 

		downIsMultiInput = frontierQuery.getLogicalInputs(downLogicalId).length > 1;
		downIsUnreplicatedSink = frontierQuery.isSink(downLogicalId) && frontierQuery.getPhysicalNodeIds(downLogicalId).size() == 1;
		upstreamRoutingController = Boolean.parseBoolean(GLOBALS.valueFor("enableUpstreamRoutingControl")) && !downIsMultiInput;
		if (upstreamRoutingController) { throw new RuntimeException("TODO."); }
	}

		
	@Override
	public ArrayList<Integer> route(long batchId) {

		Integer downOpId = null;
		
		synchronized(lock)
		{
			ArrayList<Integer> activeOpIds = getActiveOpIds();
			
			if (!activeOpIds.isEmpty())
			{
				double[] weightRanges = getWeightRanges(activeOpIds);
				
				double rand = random.nextDouble();
				logger.debug("Ranges="+Arrays.toString(weightRanges)+",rand="+rand);
				for (int i = 0; i < weightRanges.length; i++)
				{
					double range = weightRanges[i];
					if (rand <= range)
					{
						downOpId = activeOpIds.get(i);
						break;
					}
				}
				if (downOpId == null) { throw new RuntimeException("Logic error."); }
			}
			if (downOpId != lastRouted)
			{
				switchCount++;
				logger.info("Switched route from "+lastRouted + " to "+downOpId+" (switch cnt="+switchCount+")");
				lastRouted = downOpId;
			}
			
			if (downOpId != null)
			{
				ArrayList<Integer> targets = new ArrayList<>();
				targets.add(opContext.getDownOpIndexFromOpId(downOpId));
				return targets;
			}
		}
		return null;
	}

	@Override
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp) {
		if (upstreamRoutingController) { throw new RuntimeException ("Logic error."); }
		return handleDownUp(downUp, true);
	}

	/* Note don't think there is any need to actually use the expiry timer yet for WRR yet. */
	private Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp, boolean resetExpiryTimer)
	{
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId())) { throw new RuntimeException("Logic error?"); }

			logger.debug("BP Weighted RR router handling downup rctrl: "+ downUp);
			weights.put(downUp.getOpId(), downUp.getWeight());

			if (downUp.getUnmatched() != null) { throw new RuntimeException("Logic error."); }
			logger.debug("BP Weighted RR router weights= "+weights);
		}

		return null;
	}

	public Map<Integer, Set<Long>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
	{
		if (!upstreamRoutingController) { throw new RuntimeException("Logic error."); }
		synchronized(lock)
		{
			logger.debug("BPWRR router handling upstream controller weights: "+ newWeights);
			/*
			long prevUpdateTs = lastWeightUpdateTimes.get(downUpdated);
			lastWeightUpdateTimes.put(downUpdated, System.currentTimeMillis());
			weightExpiryMonitor.reset(downUpdated);
			
			if (prevUpdateTs > 0) 
			{ 
				logger.info("Weight update delay for "+downUpdated+"="+(System.currentTimeMillis() - prevUpdateTs));
			}
			*/	
			for (Integer opId : newWeights.keySet())
			{
				weights.put(opId, newWeights.get(opId));
			}
		}	
		return null;
	}

	private ArrayList<Integer> getActiveOpIds()
	{
		ArrayList<Integer> result = new ArrayList<>();
		for (Integer opId : weights.keySet())
		{		
			if (downIsUnreplicatedSink) { result.add(opId) ; break; }
			if (weights.get(opId) > 0) { result.add(opId); }
		}
		logger.debug("getActiveOpIds: Active op ids= "+result);
		return result;
	}
	
	private double[] getWeightRanges(ArrayList<Integer> activeOpIds)
	{
		double[] result = new double[activeOpIds.size()];
		double total = getTotalWeight(activeOpIds);
		double accum = 0;
		for (int i = 0; i < activeOpIds.size(); i++)
		{
			double range = weights.get(activeOpIds.get(i)) / total;
			result[i] = range + accum;
			accum  += range;
		}
		return result;
	}
	
	private double getTotalWeight(ArrayList<Integer> activeOpIds)
	{
		double total = 0;
		for (Integer opId : activeOpIds)
		{
			total += weights.get(opId); 
		}
		return total;		
	}
	
	@Override
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState) {
		throw new RuntimeException("Logic error");		
	}
	
	public Set<Long> areConstrained(Set<Long> queued)
	{
		return null;
	}
	
	public void handleDownFailed(int downOpId)
	{
		handleDownUp(new DownUpRCtrl(downOpId, -1.0, null), false);
	}
}
