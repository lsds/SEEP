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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class BackpressureRouter implements IRouter {

	private final static Logger logger = LoggerFactory.getLogger(BackpressureRouter.class);
	//private final static double INITIAL_WEIGHT = 1;
	private final static double INITIAL_WEIGHT = -1;
	private final boolean downIsMultiInput;
	private final boolean downIsUnreplicatedSink;
	private final Map<Integer, Double> weights;
	private final Map<Integer, Long> lastWeightUpdateTimes;
	private final Map<Integer, Set<Long>> unmatched;
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private ArrayList<Integer> lastOrder = null;

	private int switchCount = 0;
	private int orderChanges = 0;
	private long tLastSwitch = 0;
	private final Object lock = new Object(){};
	private final WeightExpiryMonitor weightExpiryMonitor = new WeightExpiryMonitor();
	private final boolean upstreamRoutingController;

	
	public BackpressureRouter(OperatorContext opContext) {
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
				for (Integer downOpId : unmatched.keySet())
				{
					if (unmatched.get(downOpId).contains(batchId))
					{
						if (targets == null)
						{
							targets = new ArrayList<>();
							targets.add(-1);	//Hack: allows dispatcher to distinguish constrained routing.
							logger.debug("Adding routing constraints for "+batchId);
						}
						//Don't care about weight, must at least be connected if non-empty constraints.
						//Problem: If catchup weight has changed!
						//TODO: Can perhaps optimize this, although need to be careful.
						targets.add(opContext.getDownOpIndexFromOpId(downOpId));
						logger.debug("Added routing constraint to "+downOpId);
						
						//For debugging
						Integer maxWeightDownOpId = maxWeightOpId();
						if (maxWeightDownOpId != null && !maxWeightOpId().equals(downOpId))
						{
							logger.debug("Potential target mismatch: downOpId="+downOpId+",mw="+maxWeightDownOpId);	
						}
						else if (maxWeightDownOpId != null)
						{
							logger.debug("Matching target and down op weight not null: downOpId="+downOpId+",mw="+maxWeightDownOpId);	
						}
						else { logger.debug("Max weight is null: downOpId="+downOpId);	}
					}
				}
			}
			
			//If no constrained routes, try to get based on weight.
			if (targets == null)
			{
				/*
				int opId = opContext.getOperatorStaticInformation().getOpId();
				if (opId == 0 || opId == 10 || opId == 11)
				{
					Integer fixedDownOpId = getFixedActiveDownstream();
					if (fixedDownOpId != null)
					{
						targets = new ArrayList<Integer>();
						targets.add(opContext.getDownOpIndexFromOpId(fixedDownOpId));	
					}
				}
				else if (opId == -1)
				{
					Query frontierQuery = opContext.getFrontierQuery(); 
					int logicalId = frontierQuery.getLogicalNodeId(opId);
					int height = frontierQuery.getHeight(logicalId);
					ArrayList<Integer> downOps = opContext.getDownstreamOpIdList();
					int index = (int)((batchId / Math.pow(downOps.size(), height)) % downOps.size());	
					targets = new ArrayList<Integer>();
					targets.add(index);
				}
				else
				{

				targets = weightSortedOpIds();
				}
				*/
				targets = positiveWeightSortedTargets();

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
				
				ArrayList<Integer> nextOrder = weightSortedOpIds();
				if (lastOrder != null && !nextOrder.equals(lastOrder))
				{
					orderChanges++;
					logger.info("Order change "+orderChanges+" from "+lastOrder+" -> "+nextOrder);
				}
				lastOrder = nextOrder;

				/*
				Integer downOpId = maxWeightOpId();
				
				if (downOpId != lastRouted)
				{
					switchCount++;
					long now = System.currentTimeMillis();
					long lastSwitch = 0;
					if (tLastSwitch > 0) { lastSwitch = now - tLastSwitch; }
					tLastSwitch = now;	
					logger.info("Switched route from "+lastRouted + " to "+downOpId+" (switch cnt="+switchCount+", last switch="+lastSwitch+")");
					lastRouted = downOpId;
				}
				if (downOpId != null)
				{
					targets = new ArrayList<>();
					targets.add(opContext.getDownOpIndexFromOpId(downOpId));
				}
				else { return null; }
				*/
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

	private ArrayList<Integer> positiveWeightSortedTargets()
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

			List<Map.Entry<Integer,Double>> list = new LinkedList(weights.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
			{
				public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 )
				{
					return (o1.getValue()).compareTo( o2.getValue() );
				}
			});	
		
			Collections.reverse(list);
			for (Map.Entry<Integer,Double> e : list)
			{
				if (e.getValue() > 0)
				{
					if (targets==null) { targets = new ArrayList<Integer>(); }
					targets.add(opContext.getDownOpIndexFromOpId(e.getKey()));	
				}
			}	
			return targets;
		}	
	}


	private ArrayList<Integer> weightSortedOpIds()
	{

		synchronized(lock)
		{
			if (downIsUnreplicatedSink)
			{
				ArrayList<Integer> result = new ArrayList<Integer>();
				result.addAll(weights.keySet());
				if (result.size() != 1) { throw new RuntimeException("Logic error."); }
				return result;
			}

			List<Map.Entry<Integer,Double>> list = new LinkedList(weights.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
			{
				public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 )
				{
                                	if (o1.getValue() <= 0.0 && o2.getValue() <= 0.0)
                                	{
						return o1.getKey().compareTo(o2.getKey()) * -1;
                                	}
                                	else
                                	{
						return (o1.getValue()).compareTo( o2.getValue() ) * -1;
                                	}
				}
			});	
			
			ArrayList<Integer> result = new ArrayList<Integer>();
			for (Map.Entry<Integer,Double> e : list)
			{
				result.add(e.getKey());	
			}	
			return result;
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

	private Integer getFixedActiveDownstream()
	{
		int opId = opContext.getOperatorStaticInformation().getOpId();
		synchronized(lock)
		{
			if (opId == 0)
			{
				//if (weights.get(-2) > 0) { return new Integer(-2); }
				return new Integer(-2);
			} 
			else if (opId == 10)
			{
				//if (weights.get(-190) > 0) { return new Integer(-190); }
				return new Integer(-190);
			}
			else if (opId == 11)
			{
				//if (weights.get(-189) > 0) { return new Integer(-189); }
				return new Integer(-189);
			}
		} 
		return null;
	}
}
