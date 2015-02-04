package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;

public class BackpressureRouter {

	private final Map<Integer, Double> weights;
	private final Map<Integer, Set<Long>> unmatched;
	private final static double INITIAL_WEIGHT = 1;
	
	public BackpressureRouter(ArrayList<Integer> downOps) {
		this.weights = new HashMap<>();
		this.unmatched = new HashMap<>();
		for (int downOpId : downOps)
		{
			weights.put(downOpId, INITIAL_WEIGHT);
			unmatched.put(downOpId, new HashSet<Long>());
		}
	}
	
	public Integer route(long batchId)
	{
		//TODO: Unmatched.
		return maxWeightOpId();
	}
	
	public void handleDownUp(DownUpRCtrl downUp)
	{
		if (!weights.containsKey(downUp.getOpId()))
		{
			throw new RuntimeException("Logic error?");
		}
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
			if (opWeight > maxWeight) { result = opId; }  
		}
		return result;
	}
	
	
}
