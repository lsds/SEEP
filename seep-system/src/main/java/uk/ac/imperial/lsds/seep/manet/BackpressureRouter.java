package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class BackpressureRouter {

	private final static Logger logger = LoggerFactory.getLogger(BackpressureRouter.class);
	private final static double INITIAL_WEIGHT = 1;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!

	
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
	}
	
	public Integer route(long batchId)
	{
		Integer downOpId = maxWeightOpId();
		if (downOpId != null)
		{
			return opContext.getDownOpIndexFromOpId(downOpId);
		}
		//TODO: Unmatched;
		return null;
	}
	
	public void handleDownUp(DownUpRCtrl downUp)
	{
		if (!weights.containsKey(downUp.getOpId()))
		{
			throw new RuntimeException("Logic error?");
		}
		logger.debug("BP router handling downup rctrl: "+ downUp);
		weights.put(downUp.getOpId(), downUp.getWeight());
		unmatched.put(downUp.getOpId(), downUp.getUnmatched());
	}
	
	private Integer maxWeightOpId()
	{
		Integer result = null;
		double maxWeight = 0;
		for (Integer opId : weights.keySet())
		{
			double opWeight = weights.get(opId);
			if (opWeight > maxWeight) { result = opId; maxWeight = opWeight; }
		}
		logger.debug("Backpressure router weights= "+weights);
		return result;
	}
	
	
}
