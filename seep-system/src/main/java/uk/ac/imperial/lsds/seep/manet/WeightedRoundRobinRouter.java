package uk.ac.imperial.lsds.seep.manet;

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
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class WeightedRoundRobinRouter implements IRouter {
	private final static Logger logger = LoggerFactory.getLogger(WeightedRoundRobinRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private final Random random = new Random(0);
	
	
	public WeightedRoundRobinRouter(OperatorContext opContext) {
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
	}
	
	@Override
	public Integer route(long batchId) {

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
				return opContext.getDownOpIndexFromOpId(downOpId);
			}
		}
		return null;
	}

	@Override
	public void handleDownUp(DownUpRCtrl downUp) {
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
	}

	private ArrayList<Integer> getActiveOpIds()
	{
		ArrayList<Integer> result = new ArrayList<>();
		for (Integer opId : weights.keySet())
		{		
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
}
