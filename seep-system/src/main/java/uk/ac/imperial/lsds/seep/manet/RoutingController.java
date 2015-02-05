package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;

public class RoutingController implements Runnable{

	private final static Logger logger = LoggerFactory.getLogger(RoutingController.class);
	private final static double INITIAL_WEIGHT = -1;
	private final static long MAX_WEIGHT_DELAY = 1 * 1000;
	private final CoreRE owner;
	
	private final Integer nodeId;
	private final int numLogicalInputs;
	private final Map<Integer, TreeMap<Integer, Integer>> upstreamQlens;
	private final Map<Integer, TreeMap<Integer, Double>> upstreamNetRates;
	private final double processingRate = 1; //TODO: Measure/update this dynamically?
	private final Map<Integer, Double> weights = new HashMap<>(); 
	private final Query query;

	private final Object lock = new Object(){};
	
	public RoutingController(CoreRE owner) {
		this.owner = owner;
		this.nodeId = owner.getProcessingUnit().getOperator().getOperatorId();
		this.query = owner.getProcessingUnit().getOperator().getOpContext().getMeanderQuery();
		//this.inputQueues = inputQueues;
		this.numLogicalInputs = query.getLogicalInputs(query.getLogicalNodeId(nodeId)).length;  

		this.upstreamNetRates = new HashMap<>();
		this.upstreamQlens = new HashMap<>();
		
		if (numLogicalInputs > 1)
		{
			weights.put(nodeId, INITIAL_WEIGHT);
		}
		
		for (int i = 0; i < numLogicalInputs; i++)
		{
			Set upstreamIds = query.getPhysicalInputs(query.getLogicalNodeId(nodeId))[i];
			//ArrayList<Integer> upstreamIds = owner.getProcessingUnit().getOperator().getOpContext().getUpstreamOpIdList();
			upstreamNetRates.put(i, new TreeMap<Integer, Double>());
			upstreamQlens.put(i, new TreeMap<Integer, Integer>());
			Iterator iter = upstreamIds.iterator();
			while (iter.hasNext())
			{
				Integer nextId = (Integer)iter.next();
				upstreamNetRates.get(i).put(nextId, 1.0);
				upstreamQlens.get(i).put(nextId, 0);
				if (numLogicalInputs == 1)
				{
					weights.put(nextId, INITIAL_WEIGHT);
				}
			}
		}
	}
	
	@Override
	public void run() {
		
		while(true)
		{
			Map<Integer, Double> weightsCopy = null;
			synchronized(lock)
			{
				weightsCopy = new HashMap<>(weights);
			}
			
			if (numLogicalInputs > 1)
			{
				ControlTuple ct = new ControlTuple(ControlTupleType.DOWN_UP_RCTRL, nodeId, weightsCopy.get(nodeId), new HashSet<Long>());
				owner.getControlDispatcher().sendAllUpstreams(ct);
			}
			else
			{

				for (Integer upstreamId : weightsCopy.keySet())
				{
					ControlTuple ct = new ControlTuple(ControlTupleType.DOWN_UP_RCTRL, nodeId, weightsCopy.get(upstreamId), new HashSet<Long>());
					int upOpIndex = owner.getProcessingUnit().getOperator().getOpContext().getUpOpIndexFromOpId(upstreamId);
					owner.getControlDispatcher().sendUpstream(ct, upOpIndex, false);
				}
			}
			
			synchronized(lock)
			{
				try {
					lock.wait(MAX_WEIGHT_DELAY);
				} catch (InterruptedException e) {
					//Woken up early, that's fine.
				}
			}
		}
	}

	public void handleRCtrl(UpDownRCtrl rctrl)
	{
		synchronized(lock)
		{
			if (query == null) { throw new RuntimeException("Logic error?"); }
			logger.debug("Phys node "+ nodeId + " with logical id " + query.getLogicalNodeId(nodeId) +" received updown rctrl:"+rctrl.toString());
			int inputIndex = query.getLogicalInputIndex(query.getLogicalNodeId(nodeId), query.getLogicalNodeId(rctrl.getOpId()));
			if (!upstreamQlens.get(inputIndex).containsKey(rctrl.getOpId())) { throw new RuntimeException("Logic error."); }
			this.upstreamQlens.get(inputIndex).put(rctrl.getOpId(),  new Integer(rctrl.getQlen()));
			updateWeight();
			lock.notifyAll();
		}
	}
	
	public void handleNetCostsUpdate(Map<Integer, Integer> upstreamCosts)
	{
		synchronized (lock) {
			for (int i = 0; i < upstreamNetRates.size(); i++) {
				Iterator<Integer> iter = upstreamNetRates.get(i).keySet()
						.iterator();
				while (iter.hasNext()) {
					Integer upstreamId = iter.next();
					if (!upstreamCosts.containsKey(upstreamId)) {
						throw new RuntimeException("Logic error.");
					}
					Integer cost = upstreamCosts.get(upstreamId);
					if (cost.intValue() >= GraphUtil.SUB_INFINITE_DISTANCE
							.intValue()) {
						upstreamNetRates.get(i).put(upstreamId, new Double(0));
					}
					upstreamNetRates.get(i).put(upstreamId,
							new Double(1.0 / cost.doubleValue()));
				}
			}
			updateWeight();
			lock.notifyAll();
		}
	}
	
	public DownUpRCtrl getDownUpRCtrl(Integer upstreamId)
	{
		synchronized (lock) {
			return new DownUpRCtrl(upstreamId, getWeight(upstreamId),
					getUnmatched(upstreamId));
		}
	}
	
	private int getLocalQLen()
	{
		if (owner.getDSA().getUniqueDso() != null)
		{
			return owner.getDSA().getUniqueDso().size();
		}
		else
		{
			throw new RuntimeException("TODO"); 
		}
	}
	
	private void updateWeight()
	{

			Set<Double> joinWeights = new HashSet<>();

			if (numLogicalInputs > 1) { throw new RuntimeException("TODO: What if join?"); }
			int localQlen = getLocalQLen();	//TODO: What if join!			
			for (int i = 0; i < numLogicalInputs; i++)
			{
				//Iterator<Integer> iter = query.getPhysicalInputs(query.getLogicalNodeId(nodeId))[i].iterator();
				ArrayList<Integer> upstreamIds = owner.getProcessingUnit().getOperator().getOpContext().getUpstreamOpIdList();
				Iterator<Integer> iter = upstreamIds.iterator();
				while (iter.hasNext())
				{
					Integer upstreamId = iter.next();
					double weight = computeWeight(upstreamQlens.get(i).get(upstreamId), 
							localQlen, upstreamNetRates.get(i).get(upstreamId), processingRate);
					if (numLogicalInputs == 1)
					{
						weights.put(upstreamId, weight);
					}
					else
					{
						joinWeights.add(weight);					
					}					
				}
			}
			
			if (numLogicalInputs > 1)
			{
				weights.put(nodeId, aggregate(joinWeights));
			}
	}
	
	private double computeWeight(int qLenUpstream, int qLenLocal, double netRate, double pRate)
	{
		// N.B. TODO: Need to discuss this.
		//N.B. I think the +1 here will do what we want since
		//if there is no link we will still have a weight of 0,
		//But when sending initially it will act as a gradient.
		//Not sure if it will overload the queues though?
		return (qLenUpstream + 1 - qLenLocal) * netRate * pRate;
	}
	
	private double aggregate(Set<Double> joinWeights)
	{
		//Return mean for now.
		double sum = 0;
		Iterator<Double> iter = joinWeights.iterator();
		while(iter.hasNext())
		{
			sum += ((Double)iter.next()).doubleValue();
		}
		return sum / joinWeights.size();
	}
	
	
	private double getWeight(Integer upstreamId)
	{
		logger.debug("Controller "+nodeId+" getting weight for upstream "+upstreamId+", weights="+weights);
		if (numLogicalInputs == 1)
		{
			//Special case if #inputs = 1
			return weights.get(upstreamId);
		}
		else
		{
			return weights.get(nodeId);
		}	
	}
	
	private Set<Long> getUnmatched(Integer upstreamId)
	{
		//TODO: Do this properly for joins
		if (numLogicalInputs == 1)
		{
			return new HashSet<>();
		}
		else
		{
			throw new RuntimeException("TODO");
		}		
	}

}
