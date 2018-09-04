package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.OutOfOrderBufferedBarrier;
import uk.ac.imperial.lsds.seep.runtimeengine.OutOfOrderFairBufferedBarrier;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;


//public class UpstreamRoutingController implements Runnable{
public class UpstreamRoutingController {

	private final static Logger logger = LoggerFactory.getLogger(UpstreamRoutingController.class);
	private final boolean separateControlNet = Boolean.parseBoolean(GLOBALS.valueFor("separateControlNet"));
	private final boolean piggybackControlTraffic = Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic"));
	private final boolean mergeFailureAndRoutingCtrl = Boolean.parseBoolean(GLOBALS.valueFor("mergeFailureAndRoutingCtrl"));
	private final boolean enableDummies = Boolean.parseBoolean(GLOBALS.valueFor("sendDummyDownUpControlTraffic"));
	private final boolean ignoreQueueLengths = GLOBALS.valueFor("frontierRouting").equals("weightedRoundRobin") && Boolean.parseBoolean(GLOBALS.valueFor("ignoreQueueLengths"));  
	private final boolean disableBackpressureETX = GLOBALS.valueFor("frontierRouting").equals("backpressure") && Boolean.parseBoolean(GLOBALS.valueFor("disableBackpressureETX"));

	private final static double INITIAL_WEIGHT = -1;
	//private final static double COST_THRESHOLD = 3.9;
	//private final static double COST_THRESHOLD = 4.5;
	//private final static double COST_THRESHOLD = 5.5;
	private final static double COST_THRESHOLD = Double.parseDouble(GLOBALS.valueFor("costThreshold"));
	private final long MAX_WEIGHT_DELAY;// = 1 * 1000;
	private final CoreRE owner;
	private final Integer nodeId;
	private final int logicalId;
	private final int downLogicalId;
	private final Map<Integer, Integer> downstreamQlens;
	private final Map<Integer, Double> downstreamNetRates;
	private final double processingRate = 1; //TODO: Measure/update this dynamically?
	private final Map<Integer, Double> weights = new HashMap<>(); 
	private final Query query;
	private final boolean useCostThreshold;
	private final Map<Integer, Stats.IntervalTput> tputStats = new ConcurrentHashMap<>();
	
	private final Object lock = new Object(){};
	
	public UpstreamRoutingController(CoreRE owner) {
		logger.info("Creating routing controller.");
		this.owner = owner;
		this.nodeId = owner.getProcessingUnit().getOperator().getOperatorId();
		this.query = owner.getProcessingUnit().getOperator().getOpContext().getFrontierQuery();
		//this.inputQueues = inputQueues;

		this.logicalId = query.getLogicalNodeId(nodeId);
		this.downLogicalId = query.getNextHopLogicalNodeId(logicalId);
		if (query.getLogicalInputs(downLogicalId).length > 1) { throw new RuntimeException("Logic error - UpstreamRoutingController only permitted for unary ops."); }
		
		this.useCostThreshold = query.getPhysicalNodeIds(downLogicalId).size() > 1;
		this.downstreamNetRates = new HashMap<>();
		this.downstreamQlens = new HashMap<>();

		this.MAX_WEIGHT_DELAY = Long.parseLong(GLOBALS.valueFor("routingCtrlDelay"));
		
		Set downstreamIds = query.getPhysicalNodeIds(downLogicalId);
		for (Object nextIdObj : downstreamIds)	
		{
			Integer nextId = (Integer)nextIdObj;
			downstreamNetRates.put(nextId, 1.0);
			downstreamQlens.put(nextId, 0);
			weights.put(nextId, INITIAL_WEIGHT);
		}

		logger.info("Created routing upstream controller.");
	}
	
	public void handleRCtrl(DownUpRCtrl rctrl)
	{
		synchronized(lock)
		{
			if (query == null) { throw new RuntimeException("Logic error?"); }
			logger.debug("Phys node "+ nodeId + " with logical id " + query.getLogicalNodeId(nodeId) +" received updown rctrl:"+rctrl.toString());
			//int inputIndex = query.getLogicalInputIndex(query.getLogicalNodeId(nodeId), query.getLogicalNodeId(rctrl.getOpId()));
			if (!ignoreQueueLengths) { this.downstreamQlens.put(rctrl.getOpId(),  new Integer((int)rctrl.getWeight())); }
		}

		Map<Integer, Double> weightsCopy = null;
		synchronized(lock)
		{
			//TODO: Should possibly change this to call getLocalOutputQLen without holdinng the lock.
			updateWeight(true);
			weightsCopy = new HashMap<>(weights);
		}
		logger.debug("Upstream routing controller updating router weights: "+weightsCopy);
		long tSendBegin = System.currentTimeMillis();
		owner.getProcessingUnit().getOperator().getRouter().update_highestWeights(weightsCopy, rctrl.getOpId());			
		logger.debug("Upstream routing controller updated router weights in "+(System.currentTimeMillis() - tSendBegin)+ " ms");
	}

	public void handleDownFailed(int downOpId)
	{
		handleRCtrl(new DownUpRCtrl(downOpId, -1.0, null));
	}

	/*
	public void handleRCtrl(UpDownRCtrl rctrl)
	{
		synchronized(lock)
		{
			if (query == null) { throw new RuntimeException("Logic error?"); }
			logger.info("Phys node "+ nodeId + " with logical id " + query.getLogicalNodeId(nodeId) +" received updown rctrl:"+rctrl.toString());
			int inputIndex = query.getLogicalInputIndex(query.getLogicalNodeId(nodeId), query.getLogicalNodeId(rctrl.getOpId()));
			if (!downstreamQlens.get(inputIndex).containsKey(rctrl.getOpId())) { throw new RuntimeException("Logic error."); }
			this.downstreamQlens.get(inputIndex).put(rctrl.getOpId(),  new Integer(rctrl.getQlen()));
			//updateWeight();
			lock.notifyAll();
		}
	}
	*/
	
	public void handleNetCostsUpdate(Map<Integer, Double> downstreamCosts)
	{
		synchronized (lock) {
			for (Integer downstreamId : downstreamNetRates.keySet()) {
				Double cost = downstreamCosts.get(downstreamId);
				if (tputStats.get(downstreamId) != null) { logger.info(tputStats.get(downstreamId).toString()+", cost="+cost); }

				if (cost==null || (useCostThreshold && cost > COST_THRESHOLD)) {
					cost = new Double(GraphUtil.SUB_INFINITE_DISTANCE);
					if (useCostThreshold && cost > COST_THRESHOLD)
					{ logger.warn("Cost exceeded threshold: node="+nodeId+",up="+downstreamId+",thresh="+COST_THRESHOLD+",cost="+cost); }
				}
				
				if (cost >= GraphUtil.SUB_INFINITE_DISTANCE
						.intValue()) {
					downstreamNetRates.put(downstreamId, new Double(0));
				}
				else if (disableBackpressureETX)
				{
					downstreamNetRates.put(downstreamId, new Double(1.0));
				}
				else
				{
					downstreamNetRates.put(downstreamId,
							new Double(1.0 / Math.pow(cost.doubleValue(), 2)));
				}
			}
			logger.debug("Updated downstream net rates: "+downstreamNetRates);
			//updateWeight();
			lock.notifyAll();
		}
	}

	public void handleIntervalTputUpdate(Stats.IntervalTput update)
	{
		throw new RuntimeException("TODO");
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

	private boolean areDownstreamsRoutable()
	{
		if (owner.getProcessingUnit().getDispatcher() != null)
		{
			return owner.getProcessingUnit().getDispatcher().areDownstreamsRoutable();
		}
		else
		{
			throw new RuntimeException("Logic error.");
		}
		
	}
	
	private void updateWeight(boolean downstreamsRoutable)
	{
			int localOutputQlen = getLocalOutputQLen();
			for (Object downstreamIdObj : query.getPhysicalNodeIds(downLogicalId))
			{
				Integer downstreamId = (Integer)downstreamIdObj;
				//TODO: Should we be multiplying the input q length by the processing rate?
				logger.debug("Op "+ nodeId+ " computing weight for downOpId="+downstreamId+",downstreamQlens="+downstreamQlens+",downstreamNetRates="+downstreamNetRates);

				if (ignoreQueueLengths)
				{
					double weight = downstreamNetRates.get(downstreamId);
					weights.put(downstreamId, downstreamsRoutable && downstreamQlens.get(downstreamId) >= 0 ? weight : -1);
				}
				else
				{
					double weight = computeWeight(localOutputQlen, downstreamQlens.get(downstreamId), downstreamNetRates.get(downstreamId), processingRate);
					long t = System.currentTimeMillis();	
					logger.info("t="+t+",op="+nodeId+",local output qlen="+localOutputQlen);
					logger.debug("Op "+nodeId+" downstream "+downstreamId+" weight="+weight+",qlen="+localOutputQlen+",downqlen="+downstreamQlens.get(downstreamId)+",netRate="+downstreamNetRates.get(downstreamId));
					
					weights.put(downstreamId, downstreamsRoutable ? weight : -1);
				}
			}
			
			logger.debug("Updated routing controller weights: "+ weights);
	}
	
	private double computeWeight(int qLenLocal, int qLenDownstream, double netRate, double pRateDownstream)
	{
		// N.B. TODO: Need to discuss this.
		//N.B. I think the +1 here will do what we want since
		//if there is no link we will still have a weight of 0,
		//But when sending initially it will act as a gradient.
		//Not sure if it will overload the queues though?
		if (qLenDownstream < 0) { return -1.0; }

		return (qLenLocal + 1 - qLenDownstream) * netRate * pRateDownstream;
		//return netRate * pRate;
	}
	
}
