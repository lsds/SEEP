package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;

public class ShortestPathRouter implements IRouter {

	private final static Logger logger = LoggerFactory.getLogger(ShortestPathRouter.class);
	private final OperatorContext opContext;
	private final Query query;
	private final Integer localPhysicalId;
	private Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> netTopology = null;
	private volatile Integer currentNextHop = null;
	private final Object lock = new Object(){};

	public ShortestPathRouter(OperatorContext opContext)
	{
		this.opContext = opContext;
		this.query = opContext.getFrontierQuery();
		this.localPhysicalId = opContext.getOperatorStaticInformation().getOpId();
	}

	//
	@Override
	public ArrayList<Integer> route(long batchId) {
		// TODO Auto-generated method stub
		logger.debug("Routing to next hop: "+currentNextHop);
		if (currentNextHop == null) { return null; }
		else
		{
			ArrayList<Integer> targets = new ArrayList<>();
			targets.add(currentNextHop);
			return targets;
		}
	}

	@Override
	public void updateNetTopology(Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> newNetTopology)
	{
		//TODO: Sync?
		synchronized(lock)
		{
			this.netTopology = newNetTopology;
			
			GraphUtil.logTopology(netTopology);
			//Ensure all query nodes at least exist in the net topology.
			addMissingWorkerNodes();
			GraphUtil.logTopology(netTopology);
			
			//TODO: Convert to something graph util can handle.
			Map initialAppTopology = computeInitialAppTopology(netTopology);
			Map appTopology = computeFinalAppTopology(initialAppTopology);
			logger.debug("App topology (localOpId="+localPhysicalId+": "+ appTopology);
			GraphUtil.logTopology(appTopology);
			//InetAddressNodeId sink = new InetAddressNodeId(query.getNodeAddress(query.getSinkPhysicalId()));
			Integer sink = query.getSinkPhysicalId();
			Integer nextHop = (Integer)(useBandwidthMetric() ?
					GraphUtil.nextHopBandwidth(localPhysicalId, sink, appTopology) :
						GraphUtil.nextHop(localPhysicalId, sink, appTopology));
			if (nextHop == null) 
			{ 
				if (currentNextHop != nextHop)
				{
					logger.debug("Switching next hop from "+currentNextHop +" to null");
				}
				currentNextHop = null;
			}
			else
			{
				int nextHopIndex = opContext.getDownOpIndexFromOpId(nextHop);
				if (currentNextHop == null || currentNextHop !=  nextHopIndex)
				{
					logger.debug("Switching next hop from "+currentNextHop +" to "+nextHopIndex);
				}
				currentNextHop = nextHopIndex;
			}
		}
	}

	private void addMissingWorkerNodes()
	{
		for (Object logicalIdObj : query.getLogicalNodeIds())
		{
			for(Object physicalIdObj : query.getPhysicalNodeIds((Integer)logicalIdObj))
			{
				InetAddressNodeId physAddr = new InetAddressNodeId(query.getNodeAddress((Integer)physicalIdObj));
				if (!netTopology.containsKey(physAddr))
				{
					netTopology.put(physAddr, new HashMap<InetAddressNodeId,Double>());
					//TODO: Asym links
				}
			}
		}
		
		logger.debug("TODO: What if the logical query is wrong!");
	}
	
	private Map computeFinalAppTopology(Map initialAppTopology)
	{
		//TODO: Joins.
		return initialAppTopology;
	}
	private Map computeInitialAppTopology(Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> currentNetTopology)
	{
		Map appTopology = initAppNodesMap();
		if (query.hasReplication())
		{
			appTopology = computeRawAppTopology(appTopology, currentNetTopology);
		}
		else
		{
			appTopology = computeUnreplicatedAppTopology(appTopology);
		}
		return appTopology;
	}
	private Map initAppNodesMap()
	{
		Map appNodesMap = new HashMap();
		Iterator srcIter = query.getSourcePhysicalIds().iterator();
		while (srcIter.hasNext())
		{
			appNodesMap.put(srcIter.next(), new HashMap());
		}
		appNodesMap.put(query.getSinkPhysicalId(), new HashMap());
		Iterator opIter = query.getOpPhysicalIds().iterator();
		while (opIter.hasNext())
		{
			appNodesMap.put(opIter.next(), new HashMap());
		}
		return appNodesMap;
	}
	private Map computeUnreplicatedAppTopology(Map appTopology)
	{
		Double unitCost = new Double(1);
		Iterator iter = appTopology.keySet().iterator();
		while (iter.hasNext())
		{
			Integer physicalId = (Integer)iter.next();
			Integer nextHopLogicalId = query.getNextHopLogicalNodeId(query.getLogicalNodeId(physicalId));
			//If this node isn't the sink, set its cost to next hop
			if (nextHopLogicalId != null)
			{
				Set nextHopPhysicalIds = query.getPhysicalNodeIds(nextHopLogicalId);
				if (nextHopPhysicalIds.size() != 1) throw new RuntimeException("Logic error");
				setLinkCost(physicalId, (Integer)nextHopPhysicalIds.iterator().next(), unitCost, appTopology);
			}
		}
		return appTopology;
	}
	private Map computeRawAppTopology(Map appTopology, Map currentNetTopology)
	{
		Set toProcess = new HashSet();
		Iterator srcIter = query.getSourcePhysicalIds().iterator();
		while (srcIter.hasNext())
		{
			Integer sourceId = (Integer)srcIter.next();
			//Integer sourceNodeId = query.addrToNodeId(query.getNodeAddress(sourceId));
			InetAddressNodeId sourceNodeAddr = new InetAddressNodeId(query.getNodeAddress(sourceId));
			
			Map sourceCosts = useBandwidthMetric() ? GraphUtil.widestPaths(sourceNodeAddr, currentNetTopology):GraphUtil.shortestPaths(sourceNodeAddr, currentNetTopology);
			logger.debug("Source ("+sourceId+","+sourceNodeAddr+") costs: "+ sourceCosts);
			Integer sourceLogicalId = query.getLogicalNodeId(sourceId);
			Integer nextHopLogicalId = query.getNextHopLogicalNodeId(sourceLogicalId);
			toProcess.add(nextHopLogicalId);
			Set sourceNextOpAlternatives = query.getPhysicalNodeIds(nextHopLogicalId);
			Iterator nextOpIter = sourceNextOpAlternatives.iterator();
			while(nextOpIter.hasNext())
			{
				Integer nextOp = (Integer)nextOpIter.next();
				//Integer nextOpNodeId = query.addrToNodeId(query.getNodeAddress(nextOp));
				InetAddressNodeId nextOpNodeAddr = new InetAddressNodeId(query.getNodeAddress(nextOp));
				setLinkCost(sourceId, nextOp, sourceCosts.get(nextOpNodeAddr), appTopology);
			}
		}
		//TODO Could probably define a QueryIterator class
		Set processed = new HashSet();
		while (!toProcess.isEmpty())
		{
			Set moreToProcess = new HashSet();
			Iterator queryIter = toProcess.iterator();
			while (queryIter.hasNext())
			{
				Integer logicalId = (Integer)queryIter.next();
				Integer nextHopLogicalId = query.getNextHopLogicalNodeId(logicalId);
				if (nextHopLogicalId != null)
				{
					moreToProcess.add(nextHopLogicalId);
					Set physicalIds = query.getPhysicalNodeIds(logicalId);
					Set nextHopPhysicalIds = query.getPhysicalNodeIds(nextHopLogicalId);
					Iterator physIter = physicalIds.iterator();
					while (physIter.hasNext())
					{
						Integer physicalId = (Integer)physIter.next();
						//Integer physicalNodeId = query.addrToNodeId(query.getNodeAddress(physicalId));
						InetAddressNodeId physicalNodeAddr = new InetAddressNodeId(query.getNodeAddress(physicalId));
						
						
						Map opCosts = useBandwidthMetric() ? GraphUtil.widestPaths(physicalNodeAddr, currentNetTopology) : GraphUtil.shortestPaths(physicalNodeAddr, currentNetTopology);
						logger.debug("Op ("+physicalId+","+physicalNodeAddr+") costs: "+ opCosts);
						Iterator nextHopPhysIter = nextHopPhysicalIds.iterator();
						while (nextHopPhysIter.hasNext())
						{
							Integer nextHopPhysicalId = (Integer)nextHopPhysIter.next();
							//Integer nextHopPhysicalNodeId = query.addrToNodeId(query.getNodeAddress(nextHopPhysicalId));
							InetAddressNodeId nextHopPhysicalNodeAddr = new InetAddressNodeId(query.getNodeAddress(nextHopPhysicalId));
							setLinkCost(physicalId, nextHopPhysicalId, opCosts.get(nextHopPhysicalNodeAddr), appTopology);						
						}
					}
				}
				else
				{
					//Must be the sink node.
					if (queryIter.hasNext()) throw new RuntimeException("Logic error.");
				}
				processed.add(logicalId);
			}
			toProcess.clear();
			toProcess.addAll(moreToProcess);
		}
		return appTopology;
	}
	
	private boolean useBandwidthMetric()
	{
		return false; //TODO
	}
	
	private void setLinkCost(Integer src, Integer nextHop, Object cost, Map appTopology)
	{
		((Map)appTopology.get(src)).put(nextHop, cost);
	}
	
	private Object getLinkCost(Integer src, Integer nextHop, Map appTopology)
	{
		return ((Map)appTopology.get(src)).get(nextHop);
	}

	@Override
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp) {
		throw new RuntimeException("Logic error.");
	}
	
	public Set<Long> areConstrained(Set<Long> queued)
	{
		return null;
	}
	
	public void handleDownFailed(int downOpId)
	{
		int nextHopIndex = opContext.getDownOpIndexFromOpId(downOpId);
		synchronized(lock)
		{
			if (nextHopIndex == currentNextHop)
			{
				currentNextHop = null;
			}	
		} 
	}
	public Map<Integer, Set<Long>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
	{
		throw new RuntimeException("Logic error."); 
	}
}

