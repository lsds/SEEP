package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class PowerOf2ChoicesRouter implements IRouter {

	private final static Logger logger = LoggerFactory.getLogger(PowerOf2ChoicesRouter.class);
	//private final static double INITIAL_WEIGHT = 1;
	private final static double INITIAL_WEIGHT = -1;
	private final boolean downIsMultiInput;
	private final boolean downIsUnreplicatedSink;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Long> lastWeightUpdateTimes;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private final Random random = new Random(0);
	private Integer lastRouted = null;
	private ArrayList<Integer> lastOrder = null;

	private int switchCount = 0;
	private int orderChanges = 0;
	private long tLastSwitch = 0;
	private final Object lock = new Object(){};
	private final WeightExpiryMonitor weightExpiryMonitor = new WeightExpiryMonitor();
	private final boolean upstreamRoutingController;

	
	public PowerOf2ChoicesRouter(OperatorContext opContext) {
		this.weights = new HashMap<>();
		this.lastWeightUpdateTimes = new HashMap<>();
		this.unmatched = new HashMap<>();
		this.opContext = opContext;
		ArrayList<Integer> downOps = opContext.getDownstreamOpIdList();
		for (int downOpId : downOps)
		{
			weights.put(downOpId, INITIAL_WEIGHT);
			lastWeightUpdateTimes.put(downOpId, -1L);
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
	
	public ArrayList<Integer> route(long batchId)
	{
		ArrayList<Integer> targets = null;
		synchronized(lock)
		{
			if (downIsMultiInput && !downIsUnreplicatedSink)
			{
				throw new RuntimeException("TODO: See BackpressureRouter.");
			}
			
			//If no constrained routes, try to get based on weight.
			if (targets == null)
			{
				targets = powerOf2ChoicesTarget();

				Integer downOpId = targets == null ? null : opContext.getDownOpIdFromIndex(targets.get(0));
				if (!Objects.equals(downOpId, lastRouted))
				{
					switchCount++;
					long now = System.currentTimeMillis();
					long lastSwitch = 0;
					if (tLastSwitch > 0) { lastSwitch = now - tLastSwitch; }
					tLastSwitch = now;	
					logger.info("Switched route from "+lastRouted + " to "+downOpId+" (switch cnt="+switchCount+", last switch="+lastSwitch+")");
					lastRouted = downOpId;
				}
			}
		}
		return targets;
	}
	

	public Map<Integer, Set<Long>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
	{
		synchronized(lock)
		{
			logger.debug("BP router handling upstream controller weights: "+ newWeights);
			long prevUpdateTs = lastWeightUpdateTimes.get(downUpdated);
			lastWeightUpdateTimes.put(downUpdated, System.currentTimeMillis());
			weightExpiryMonitor.reset(downUpdated);
			
			if (prevUpdateTs > 0) 
			{ 
				logger.info("Weight update delay for "+downUpdated+"="+(System.currentTimeMillis() - prevUpdateTs));
			}
			
			for (Integer opId : newWeights.keySet())
			{
				weights.put(opId, newWeights.get(opId));
			}
		}	
		return null;
	}

	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp)
	{
		if (upstreamRoutingController) { throw new RuntimeException ("Logic error."); }
		return handleDownUp(downUp, true);
	}

	public void handleDownFailed(int downOpId)
	{
		handleDownUp(new DownUpRCtrl(downOpId, -1.0, null), false);
	}
	
	private Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp, boolean resetExpiryTimer)
	{
		Map<Integer, Set<Long>> newConstraints = null;
		long tStart = System.currentTimeMillis();
		synchronized(lock)
		{
			if (!weights.containsKey(downUp.getOpId()))
			{
				throw new RuntimeException("Logic error?");
			}
			logger.debug("BP router handling downup rctrl: "+ downUp);
			long tAcquire = System.currentTimeMillis();
			logger.trace("BP router handling downup rctrl acquired lock in " + (tAcquire - tStart));
			long prevUpdateTs = lastWeightUpdateTimes.get(downUp.getOpId());
			lastWeightUpdateTimes.put(downUp.getOpId(), System.currentTimeMillis());
			if (resetExpiryTimer)
			{
				weightExpiryMonitor.reset(downUp.getOpId());
			}
			
			if (prevUpdateTs > 0) 
			{ 
				logger.info("Weight update delay for "+downUp.getOpId()+"="+(System.currentTimeMillis() - prevUpdateTs));
			}
			
			weights.put(downUp.getOpId(), downUp.getWeight());
			
			if (!resetExpiryTimer)
			{
				Integer newMaxWeightOpId = maxWeightOpId();
				if (maxWeightOpId() != null)
				{
					logger.info("Downstream timed out with alternative max weight downstream="+newMaxWeightOpId);
				}
				else { logger.info("Downstream timed out with no alternative downstream."); }
			}
			
			logger.debug("handleDownUp:Node "+opContext.getOperatorStaticInformation().getOpId()+" backpressure router weights= "+weights);
			Set<Long> newUnmatched = downUp.getUnmatched();
			if (newUnmatched != null)
			{
				//TODO: Tmp hack: Null here indicates a local update because
				//an attempt to send a q length msg upstream failed - should
				//clean it up to perhaps use a different method.
				Set<Long> oldUnmatched = unmatched.get(downUp.getOpId());
				boolean changed = unmatchedChanged(oldUnmatched, newUnmatched);
				logger.debug("Unmatched changed for "+downUp.getOpId()+"="+changed+", old="+oldUnmatched+",new="+newUnmatched);
				unmatched.put(downUp.getOpId(), newUnmatched);
				
				if (changed) {
					newConstraints = new HashMap<Integer, Set<Long>>();
					newConstraints.put(downUp.getOpId(), new HashSet<>(newUnmatched));
				}
			}
			else
			{
				unmatched.get(downUp.getOpId()).clear();
			}
		}
		logger.debug("BP router handled downup rctrl in " + (System.currentTimeMillis() - tStart));
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
				if (downIsUnreplicatedSink) { return opId; }
				double opWeight = weights.get(opId);
				if (opWeight > maxWeight) { result = opId; maxWeight = opWeight; }
			}
			logger.debug("maxWeight:Node "+opContext.getOperatorStaticInformation().getOpId()+" backpressure router weights= "+weights);
		}
		return result;
	}


	private ArrayList<Integer> powerOf2ChoicesTarget()
	{
		synchronized(lock)
		{
			ArrayList<Integer> targets = null;
			if (downIsUnreplicatedSink)
			{
				targets = new ArrayList<Integer>();
				for (Integer opId : weights.keySet()) 
				{ 
					targets.add(opContext.getDownOpIndexFromOpId(opId)); 
					return targets;
				}
				throw new RuntimeException("Logic error.");
			}

			ArrayList<Map.Entry<Integer,Double>> list = new ArrayList(weights.entrySet());
			if (list.size() < 2) { throw new RuntimeException("Logic error."); }
			int firstChoice = random.nextInt(list.size());
			int secondChoice = random.nextInt(list.size());
			while (firstChoice == secondChoice) { secondChoice = random.nextInt(list.size()); }
			int highestWeightChoice = list.get(firstChoice).getValue() > list.get(secondChoice).getValue() ? firstChoice : secondChoice;
			if (list.get(highestWeightChoice).getValue() > 0)
			{
				targets = new ArrayList<Integer>();
				targets.add(opContext.getDownOpIndexFromOpId(list.get(highestWeightChoice).getKey()));
			}
			return targets;
		}
	}

	@Override
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState) {
		throw new RuntimeException("Logic error");		
	}
	
	public Set<Long> areConstrained(Set<Long> queued)
	{

		if (queued == null || queued.isEmpty() || !downIsMultiInput) { return null; }
		logger.debug("Checking constrained for queued: "+queued);
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
			logger.debug("Constrained in queue: "+constraints);
			return constraints;
		}
	}
	
	private boolean unmatchedChanged(Set<Long> oldUnmatched, Set<Long> newUnmatched)
	{
		if (oldUnmatched == null && newUnmatched == null) { return false;}		
		if ((oldUnmatched == null && !newUnmatched.isEmpty()) || 
				(newUnmatched == null && !oldUnmatched.isEmpty())) { return true;}
		if (oldUnmatched.size() != newUnmatched.size()) { return true; }
		
		for (Long unmatched : oldUnmatched) 
		{ 
			if (!newUnmatched.contains(unmatched)) { return true; }
		}
		return false;
	}
	
	private class WeightExpiryMonitor
	{
		final static long DOWNSTREAM_MAX_WEIGHT_DELAY = 2 * 1000;
		final static long WEIGHT_TIMEOUT = 2 * DOWNSTREAM_MAX_WEIGHT_DELAY;
		final Timer timer = new Timer(true);
		final Map<Integer, TimerTask> currentTasks = new HashMap<>();
		public void reset(final int opId)
		{
			if (currentTasks.containsKey(opId))
			{
				currentTasks.remove(opId).cancel();
			}
			TimerTask timeoutTask = new TimerTask() 
			{ 
				public void run() 
				{  
					logger.warn("Down op weight "+opId+" expired.");
					//TODO: Bit wary about causing some kind of deadlock here.
					//Also, never actually notify the dispatcher about the change.
					handleDownUp(new DownUpRCtrl(opId, -1.0, null), false);
				} 
			};
			
			currentTasks.put(opId, timeoutTask);
			timer.schedule(timeoutTask, WEIGHT_TIMEOUT);
		}
	}
}
