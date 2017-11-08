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
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.GLOBALS;

/**
 * Weighted Round Robin supports only upstream routing control, since for now downstream routing
 * controllers incorporate queue differentials into their weight calculations.
 */
public class WeightedRoundRobinRouter implements IRouter {
	private final static Logger logger = LoggerFactory.getLogger(WeightedRoundRobinRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Timestamp>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private final Random random = new Random(0);
	private final boolean upstreamRoutingController;
	private final boolean downIsMultiInput;
	private final boolean downIsUnreplicatedSink;
	
	
	public WeightedRoundRobinRouter(OperatorContext opContext) {
		this.weights = new HashMap<>();
		this.unmatched = new HashMap<>();
		this.opContext = opContext;
		ArrayList<Integer> downOps = opContext.getDownstreamOpIdList();
		for (int downOpId : downOps)
		{
			weights.put(downOpId, INITIAL_WEIGHT);
			unmatched.put(downOpId, new HashSet<Timestamp>());
		}
		logger.info("Initial weights: "+weights);
		Query meanderQuery = opContext.getMeanderQuery(); 
		int logicalId = meanderQuery.getLogicalNodeId(opContext.getOperatorStaticInformation().getOpId());
		int downLogicalId = meanderQuery.getNextHopLogicalNodeId(logicalId); 

		downIsMultiInput = meanderQuery.getLogicalInputs(downLogicalId).length > 1;
		downIsUnreplicatedSink = meanderQuery.isSink(downLogicalId) && meanderQuery.getPhysicalNodeIds(downLogicalId).size() == 1;
		upstreamRoutingController = Boolean.parseBoolean(GLOBALS.valueFor("enableUpstreamRoutingControl")) && !downIsMultiInput;
	}

		
	@Override
	public ArrayList<Integer> route(Timestamp batchId) {

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
	public Map<Integer, Set<Timestamp>> handleDownUp(DownUpRCtrl downUp) {
		if (upstreamRoutingController) { throw new RuntimeException ("Logic error."); }
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId()))
			{
				throw new RuntimeException("Logic error?");
			}
			logger.debug("Weighted rr router handling downup rctrl: "+ downUp);
			weights.put(downUp.getOpId(), downUp.getWeight());
			if (downUp.getUnmatched() != null)
			{
				//TODO: Tmp hack: Null here indicates a local update because
				//an attempt to send a q length msg upstream failed - should
				//clean it up to perhaps use a different method.
				unmatched.put(downUp.getOpId(), downUp.getUnmatched());
			}
			logger.debug("Weighted rr router weights= "+weights);
		}
		throw new RuntimeException("TODO");
	}

	public Map<Integer, Set<Timestamp>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
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
	
	public Set<Timestamp> areConstrained(Set<Timestamp> queued)
	{
		return null;
	}
	
	public void handleDownFailed(int downOpId)
	{
		throw new RuntimeException("TODO"); 
	}
}
