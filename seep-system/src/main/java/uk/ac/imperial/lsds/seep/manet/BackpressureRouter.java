package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};

	
	public BackpressureRouter(OperatorContext opContext) {
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
		Map<Integer, Set<Long>> newConstraints = null;
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId()))
			{
				throw new RuntimeException("Logic error?");
			}
			logger.debug("BP router handling downup rctrl: "+ downUp);
			weights.put(downUp.getOpId(), downUp.getWeight());
			logger.debug("Backpressure router weights= "+weights);
			if (downUp.getUnmatched() != null)
			{
				//TODO: Tmp hack: Null here indicates a local update because
				//an attempt to send a q length msg upstream failed - should
				//clean it up to perhaps use a different method.
				unmatched.put(downUp.getOpId(), downUp.getUnmatched());
				newConstraints = new HashMap<Integer, Set<Long>>();
				newConstraints.put(downUp.getOpId(), downUp.getUnmatched());
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
		logger.trace("Checking constrained for queued: "+queued);
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
			logger.trace("Constrained in queue: "+constraints);
			return constraints;
		}
	}
}
