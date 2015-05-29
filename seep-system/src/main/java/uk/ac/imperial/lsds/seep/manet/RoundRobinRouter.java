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
					if (weights.get(downOps.get(nextRoundRobinIndex)) > 0)
					{
						downOpId = downOps.get(nextRoundRobinIndex);
						nextRoundRobinIndex = ((nextRoundRobinIndex + 1) % downOps.size());
						break;
					}
					else 
					{ 
						nextRoundRobinIndex = ((nextRoundRobinIndex + 1) % downOps.size());
						if (nextRoundRobinIndex == initialIndex) { throw new RuntimeException("Logic error - no op found!"); }
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
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp) {
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId()))
			{
				throw new RuntimeException("Logic error?");
			}
			logger.debug("RR router handling downup rctrl: "+ downUp);
			weights.put(downUp.getOpId(), downUp.getWeight());
			if (downUp.getUnmatched() != null)
			{
				//TODO: Tmp hack: Null here indicates a local update because
				//an attempt to send a q length msg upstream failed - should
				//clean it up to perhaps use a different method.
				unmatched.put(downUp.getOpId(), downUp.getUnmatched());
			}
			logger.debug("RR router weights= "+weights);
		}
		throw new RuntimeException("TODO");
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
			logger.debug("maxWeight: Round robin router weights= "+weights);
		}
		return result;
	}
	
	
	@Override
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState) {
		throw new RuntimeException("Logic error");		
	}
}
