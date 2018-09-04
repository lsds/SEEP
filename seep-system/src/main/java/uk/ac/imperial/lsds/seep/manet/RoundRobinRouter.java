package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.GLOBALS;


/**
 * Round Robin supports both upstream and downstream routing control, since it doesn't 
 * care what the weights represent only whether they are positive. 
 */
public class RoundRobinRouter implements IRouter {
	private final static Logger logger = LoggerFactory.getLogger(RoundRobinRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private int nextRoundRobinIndex = 0;
	private final ArrayList<Integer> downOps;
	private final boolean upstreamRoutingController;
	private final boolean downIsMultiInput;
	private final boolean downIsUnreplicatedSink;
	
	
	
	public RoundRobinRouter(OperatorContext opContext) {
		this.weights = new HashMap<>();
		this.unmatched = new HashMap<>();
		this.opContext = opContext;
		this.downOps = opContext.getDownstreamOpIdList();
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
	}
	
	@Override
	public ArrayList<Integer> route(long batchId) {
		synchronized(lock)
		{
			Integer downOpId = maxWeightOpId();
			
			if (downOpId != null)
			{
				//At least one downstream is up
				int initialIndex = nextRoundRobinIndex;
				while (true)
				{
					if (weights.get(downOps.get(nextRoundRobinIndex)) > 0 || downIsUnreplicatedSink)
					{
						downOpId = downOps.get(nextRoundRobinIndex);
						nextRoundRobinIndex = ((nextRoundRobinIndex + 1) % downOps.size());
						break;
					}
					else 
					{ 
						nextRoundRobinIndex = ((nextRoundRobinIndex + 1) % downOps.size());
						if (nextRoundRobinIndex == initialIndex && !downIsUnreplicatedSink) { throw new RuntimeException("Logic error - no op found!"); }
					}
	
				}
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
		//TODO: Unmatched;
		return null;		
	}

	@Override
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp)
	{
		if (upstreamRoutingController) { throw new RuntimeException ("Logic error."); }
		return handleDownUp(downUp, true);
	}

	/* Note don't think there is any need to actually use the expiry timer yet for RR yet. */
	private Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp, boolean resetExpiryTimer)
	{
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId())) { throw new RuntimeException("Logic error?"); }

			logger.debug("RR router handling downup rctrl: "+ downUp);
			weights.put(downUp.getOpId(), downUp.getWeight());

			if (downUp.getUnmatched() != null) { throw new RuntimeException("Logic error."); }
			logger.debug("RR router weights= "+weights);
		}

		return null;
	}
	
	private Integer maxWeightOpId()
	{
		Integer result = null;
		double maxWeight = 0;
		synchronized(lock)
		{
			for (Integer opId : weights.keySet())
			{
				if (downIsUnreplicatedSink) { return opId; }
				double opWeight = weights.get(opId);
				if (opWeight > maxWeight) { result = opId; maxWeight = opWeight; }
			}
			logger.debug("maxWeight: Round robin router weights= "+weights);
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
		return null;
	}
	
	public void handleDownFailed(int downOpId)
	{
		handleDownUp(new DownUpRCtrl(downOpId, -1.0, null), false);
	}

	public Map<Integer, Set<Long>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
	{
		if (!upstreamRoutingController) { throw new RuntimeException("Logic error."); }
		synchronized(lock)
		{
			logger.debug("WRR router handling upstream controller weights: "+ newWeights);
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
}
