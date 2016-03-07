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

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutOfOrderBufferedBarrier;

public class RoutingController implements Runnable{

	private final static Logger logger = LoggerFactory.getLogger(RoutingController.class);
	private final static double INITIAL_WEIGHT = -1;
	//private final static double COST_THRESHOLD = 3.9;
	//private final static double COST_THRESHOLD = 4.5;
	//private final static double COST_THRESHOLD = 5.5;
	private final static double COST_THRESHOLD = Double.parseDouble(GLOBALS.valueFor("costThreshold"));
	private final long MAX_WEIGHT_DELAY;// = 1 * 1000;
	private final CoreRE owner;
	private final Integer nodeId;
	private final int numLogicalInputs;
	private final Map<Integer, TreeMap<Integer, Integer>> upstreamQlens;
	private final Map<Integer, TreeMap<Integer, Double>> upstreamNetRates;
	private final double processingRate = 1; //TODO: Measure/update this dynamically?
	private final Map<Integer, Double> weights = new HashMap<>(); 
	private final Query query;
	private final boolean useCostThreshold;
	
	private final Object lock = new Object(){};
	
	public RoutingController(CoreRE owner) {
		this.owner = owner;
		this.nodeId = owner.getProcessingUnit().getOperator().getOperatorId();
		this.query = owner.getProcessingUnit().getOperator().getOpContext().getMeanderQuery();
		//this.inputQueues = inputQueues;
		this.numLogicalInputs = query.getLogicalInputs(query.getLogicalNodeId(nodeId)).length;
		
		this.useCostThreshold = query.getPhysicalNodeIds(query.getLogicalNodeId(nodeId)).size() > 1;
		this.upstreamNetRates = new HashMap<>();
		this.upstreamQlens = new HashMap<>();

		if (numLogicalInputs > 1)
		{
			//this.MAX_WEIGHT_DELAY = 10 * 1000;
			//this.MAX_WEIGHT_DELAY = 2 * 1000;
			this.MAX_WEIGHT_DELAY = Long.parseLong(GLOBALS.valueFor("routingCtrlDelay"));
			weights.put(nodeId, INITIAL_WEIGHT);
		}
		else
		{
			//this.MAX_WEIGHT_DELAY = 1 * 50;
			//this.MAX_WEIGHT_DELAY = 1 * 25;
			this.MAX_WEIGHT_DELAY = Long.parseLong(GLOBALS.valueFor("routingCtrlDelay"));
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
	
		long tLast = System.currentTimeMillis();	
		while(true)
		{
			Map<Integer, Double> weightsCopy = null;
			synchronized(lock)
			{
				updateWeight();
				weightsCopy = new HashMap<>(weights);
			}
			
			logger.info("Routing controller sending weights upstream: "+weightsCopy);
			
			long tSendBegin = System.currentTimeMillis();
			if (numLogicalInputs > 1)
			{
				ArrayList<RangeSet<Long>> routingConstraints = ((OutOfOrderBufferedBarrier)owner.getDSA().getUniqueDso()).getRoutingConstraints();
				for (Integer upstreamId : owner.getProcessingUnit().getOperator().getOpContext().getUpstreamOpIdList())
				{
					int logicalInputIndex = query.getLogicalInputIndex(query.getLogicalNodeId(nodeId), query.getLogicalNodeId(upstreamId));
					//N.B. Sending the *aggregate* weight across all upstreams.
					logger.info("Routing controller sending constraints upstream op "+upstreamId+": "+routingConstraints.get(logicalInputIndex));
					ControlTuple ct = new ControlTuple(ControlTupleType.DOWN_UP_RCTRL, nodeId, weightsCopy.get(nodeId), routingConstraints.get(logicalInputIndex));
					int upOpIndex = owner.getProcessingUnit().getOperator().getOpContext().getUpOpIndexFromOpId(upstreamId);
					owner.getControlDispatcher().sendUpstream(ct, upOpIndex, false);
				}
			}
			else
			{

				for (Integer upstreamId : weightsCopy.keySet())
				{
					RangeSet<Long> empty = TreeRangeSet.create();
					//ControlTuple ct = new ControlTuple(ControlTupleType.DOWN_UP_RCTRL, nodeId, weightsCopy.get(upstreamId), empty);
					double weight = weightsCopy.get(upstreamId);
					//if (nodeId == 1 && upstreamId == 10 || nodeId == 110 && upstreamId == 0 || nodeId == -2 && upstreamId == 110 || nodeId == -190 && upstreamId == 1) { weight = 0.0 ; } 
					//if (nodeId == 1 && upstreamId == 0 || nodeId == 110 && upstreamId == 10 || nodeId == -2 && upstreamId == 110 || nodeId == -190 && upstreamId == 1) { weight = 0.0 ; } 
					ControlTuple ct = new ControlTuple(ControlTupleType.DOWN_UP_RCTRL, nodeId, weight, empty);
					int upOpIndex = owner.getProcessingUnit().getOperator().getOpContext().getUpOpIndexFromOpId(upstreamId);
					owner.getControlDispatcher().sendUpstream(ct, upOpIndex, false);
				}
			}
			logger.info("Routing controller send weights upstream in "+(System.currentTimeMillis() - tSendBegin)+ " ms, since last="+(System.currentTimeMillis()-tLast)+" ms");
			long tStart = System.currentTimeMillis();
			tLast = tStart;
			long tNow = tStart;
			while (tNow - tStart < MAX_WEIGHT_DELAY)
			{
				synchronized(lock)
				{
					try {
						lock.wait(MAX_WEIGHT_DELAY - (tNow - tStart));
					} catch (InterruptedException e) {
						//Woken up early, that's fine.
					}
				}
				tNow = System.currentTimeMillis();
			}
		
		/*
		synchronized(lock)
		{
			try {
				lock.wait(MAX_WEIGHT_DELAY);
			} catch (InterruptedException e) {
				//Woken up early, that's fine.
			}
		}
		*/
		}
	}

	public void handleRCtrl(UpDownRCtrl rctrl)
	{
		synchronized(lock)
		{
			if (query == null) { throw new RuntimeException("Logic error?"); }
			logger.info("Phys node "+ nodeId + " with logical id " + query.getLogicalNodeId(nodeId) +" received updown rctrl:"+rctrl.toString());
			int inputIndex = query.getLogicalInputIndex(query.getLogicalNodeId(nodeId), query.getLogicalNodeId(rctrl.getOpId()));
			if (!upstreamQlens.get(inputIndex).containsKey(rctrl.getOpId())) { throw new RuntimeException("Logic error."); }
			this.upstreamQlens.get(inputIndex).put(rctrl.getOpId(),  new Integer(rctrl.getQlen()));
			//updateWeight();
			lock.notifyAll();
		}
	}
	
	public void handleNetCostsUpdate(Map<Integer, Double> upstreamCosts)
	{
		synchronized (lock) {
			for (int i = 0; i < upstreamNetRates.size(); i++) {
				Iterator<Integer> iter = upstreamNetRates.get(i).keySet()
						.iterator();
				while (iter.hasNext()) {
					Integer upstreamId = iter.next();
					Double cost = upstreamCosts.get(upstreamId);
					if (cost==null || (useCostThreshold && cost > COST_THRESHOLD)) {
						cost = new Double(GraphUtil.SUB_INFINITE_DISTANCE);
					}
					
					if (cost >= GraphUtil.SUB_INFINITE_DISTANCE
							.intValue()) {
						upstreamNetRates.get(i).put(upstreamId, new Double(0));
					}
					else
					{
						upstreamNetRates.get(i).put(upstreamId,
								new Double(1.0 / cost.doubleValue()));
					}
				}
			}
			logger.info("Updated upstream net rates: "+upstreamNetRates);
			//updateWeight();
			lock.notifyAll();
		}
	}
	
	private int getLocalOutputQLen()
	{
		if (owner.getProcessingUnit().getDispatcher() != null)
		{
			/*
			if (owner.getProcessingUnit().getOperator().getOpContext().isSink())
			{ throw new RuntimeException("Logic error."); }
			*/
			int localOutputQlen = owner.getProcessingUnit().getDispatcher().getTotalQlen();
			//logger.info("Op "+nodeId+" local output qlen=" + localOutputQlen);
			return localOutputQlen;
		}
		else
		{
			throw new RuntimeException("Logic error.");
		}
	}
	
	private void updateWeight()
	{
			Map<Integer, Integer> localInputQlens = null;
			if (numLogicalInputs > 1)
			{
				 localInputQlens = ((OutOfOrderBufferedBarrier)owner.getDSA().getUniqueDso()).sizes();
			}
			else
			{
				//TODO: Just change dsa inf to have a single sizes method.
				localInputQlens = new HashMap<Integer, Integer>();
				localInputQlens.put(-1,owner.getDSA().getUniqueDso().size());
			}
			
			//int localInputQlen = getLocalInputQLen();	//TODO: What if join!
			int localOutputQlen = getLocalOutputQLen();
			ArrayList<Set<Double>> joinWeights = new ArrayList<>();
			for (int i = 0; i < numLogicalInputs; i++)
			{
				//Iterator<Integer> iter = query.getPhysicalInputs(query.getLogicalNodeId(nodeId))[i].iterator();
				joinWeights.add(new HashSet<Double>());
				ArrayList<Integer> upstreamIds = new ArrayList<>(query.getPhysicalInputs(query.getLogicalNodeId(nodeId))[i]);
				Iterator<Integer> iter = upstreamIds.iterator();
				while (iter.hasNext())
				{
					Integer upstreamId = iter.next();
					int localTotalInputQlen = localInputQlens.get(-1);

					//TODO: Should we be multiplying the input q length by the processing rate?
					logger.info("Computing weight for input="+i+", upOpId="+upstreamId+",upstreamQlens="+upstreamQlens+",upstreamNetRates="+upstreamNetRates);
					double weight = computeWeight(upstreamQlens.get(i).get(upstreamId), 
							localTotalInputQlen + localOutputQlen, upstreamNetRates.get(i).get(upstreamId), processingRate);
					
					logger.info("Op "+nodeId+" total qlen="+(localTotalInputQlen+localOutputQlen)+",inputq="+localTotalInputQlen+",outputq="+localOutputQlen);
					logger.info("Op "+nodeId+" upstream "+upstreamId+" weight="+weight+",qlen="+upstreamQlens.get(i).get(upstreamId)+",netRate="+upstreamNetRates.get(i).get(upstreamId));
					
					if (numLogicalInputs == 1)
					{
						weights.put(upstreamId, weight);
					}
					else
					{
						joinWeights.get(i).add(weight);
						//Also compute per upstream weight for routing constraints
						//TODO: Debatable what should actually be used here.
						int localPerInputQlen = localInputQlens.get(i);
						weight = computeWeight(upstreamQlens.get(i).get(upstreamId), 
								localPerInputQlen + localOutputQlen, upstreamNetRates.get(i).get(upstreamId), processingRate);
						weights.put(upstreamId, weight);
					}					
				}
			}
			
			if (numLogicalInputs > 1)
			{
				weights.put(nodeId, aggregate(joinWeights));
			}
			logger.info("Updated routing controller weights: "+ weights);
	}
	
	private double computeWeight(int qLenUpstream, int qLenLocal, double netRate, double pRate)
	{
		// N.B. TODO: Need to discuss this.
		//N.B. I think the +1 here will do what we want since
		//if there is no link we will still have a weight of 0,
		//But when sending initially it will act as a gradient.
		//Not sure if it will overload the queues though?
		return (qLenUpstream + 1 - qLenLocal) * netRate * pRate;
		//return netRate * pRate;
	}
	
	private double aggregate(ArrayList<Set<Double>> joinWeights)
	{
		ArrayList<Double> perInputAggregates = aggregateInputs(joinWeights);
		
		//Return mean for now.
		double sum = 0;
		for (Double aggregatedInputWeight : perInputAggregates)
		{
			sum += aggregatedInputWeight;
		}
		logger.debug("Aggregate weight = "+ (sum/perInputAggregates.size()));
		return sum / perInputAggregates.size();
	}
	
	private ArrayList<Double> aggregateInputs(ArrayList<Set<Double>> joinWeights)
	{
		//Get the max across all upstreams for this logical input.
		ArrayList<Double> result = new ArrayList<>();
		for (Set<Double> inputWeights : joinWeights)
		{
			double max = 0;
			for (Double upstreamWeight : inputWeights)
			{
				max = Math.max(upstreamWeight, max);
			}
			result.add(max);
		}
		return result;
	}
}
