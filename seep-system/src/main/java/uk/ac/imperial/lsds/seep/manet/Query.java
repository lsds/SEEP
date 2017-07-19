package uk.ac.imperial.lsds.seep.manet;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Query implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = -5192884050421145835L;
	private final static Logger logger = LoggerFactory.getLogger(Query.class);
	private final TreeMap logicalTopology;  //TreeMap<Integer -> Integer[]
	private final TreeMap log2phys; //TreeMap<Integer -> Set<Integer>>
	private final Map phys2log; //HashMap<Integer -> Integer>
	private final Map<Integer, InetAddress> phys2addr;
	private final Map<InetAddress, Set<Integer>> addr2phys;
	private final Set parallelized;



	public Query(TreeMap logicalTopology, TreeMap log2phys, Map<Integer, InetAddress> phys2addr)
	{
		this(logicalTopology, log2phys, new HashSet(), phys2addr);
	}

	/* N.B. Logical node ids MUST be in a topological order */
	public Query(TreeMap logicalTopology, TreeMap log2phys, Set parallelized, Map<Integer, InetAddress> phys2addr)
	{
		this.logicalTopology = logicalTopology;
		logger.info("Number of logical nodes:"+logicalTopology.size());
		this.log2phys = log2phys;
		this.parallelized = parallelized;
		this.phys2log = computePhys2log();
		this.phys2addr = phys2addr;
		this.addr2phys = computeAddr2phys();

		logger.info("Logical topology: "+ logicalTopologyToString());
		logger.info("Log2phys: "+ log2physToString());
		logger.info("Parallelizable: "+ parallelized);

		//TODO validation.
	}

	private Map computePhys2log()
	{
		Map result = new HashMap();
		Iterator logIter = log2phys.keySet().iterator();
		while (logIter.hasNext())
		{
			Integer logicalId = (Integer)logIter.next();
			Iterator physIter = ((Set)log2phys.get(logicalId)).iterator();
			while(physIter.hasNext())
			{
				Integer physicalId = (Integer)physIter.next();
				if (result.containsKey(physicalId))
				{
					logger.error("computePhys2log: result="+result+",physId="+physicalId);
					throw new RuntimeException("Logic error.");
				
				}
				result.put(physicalId, logicalId);
			}
		}

		return result;
	}

	private Map<InetAddress, Set<Integer>> computeAddr2phys()
	{
		logger.info("Computing addr2phys");
		Map<InetAddress, Set<Integer>> result = new HashMap<>();
		for (Integer id : phys2addr.keySet())
		{
			InetAddress addr = phys2addr.get(id);
			if (!result.containsKey(addr))
			{
				result.put(addr, new HashSet<Integer>());
			}
			result.get(addr).add(id);
		}
		return result;
	}
	
	//TODO: Change this name to getPhysicalIds
	public Set getPhysicalNodeIds(Integer logicalId)
	{
		return (Set)log2phys.get(logicalId);
	}

	//TODO: Change this name to getLogicalId
	public Integer getLogicalNodeId(Integer physicalId)
	{
		return (Integer)phys2log.get(physicalId);
	}

	public InetAddress getNodeAddress(Integer physicalId)
	{
		return phys2addr.get(physicalId);
	}

	//TODO: Change this name to getLogicalIds
	public Set getLogicalNodeIds()
	{
		return logicalTopology.keySet();
	}

	public Integer[] getLogicalInputs(Integer logicalId)
	{
		return (Integer[])logicalTopology.get(logicalId);
	}

	public Integer getLogicalInputIndex(Integer logicalId, Integer logicalInputId)
	{
		Integer[] logicalInputs = getLogicalInputs(logicalId);
		for (int i = 0; i < logicalInputs.length; i++)
		{
			if (logicalInputs[i].equals(logicalInputId))
			{
				return new Integer(i);
			}
		}

		throw new RuntimeException("Logical id "+logicalId+" does not have a logical input with id "+logicalInputId);
	}

	public Set[] getPhysicalInputs(Integer logicalId)
	{
		Integer[] logicalInputs = (Integer[])logicalTopology.get(logicalId);

		if (logicalInputs.length > 0)
		{
			Set[] result = new Set[logicalInputs.length];
			for (int i = 0; i < logicalInputs.length; i++)
			{
				result[i] = (Set)log2phys.get(logicalInputs[i]);
			}

			return result;
		}
		else
		{
			return new Set[]{};
		}
	}


	public boolean isJoin(Integer logicalId)
	{
		return getPhysicalInputs(logicalId).length > 1;
	}

	public boolean isSource(Integer logicalId)
	{
		return logicalId != null && getLogicalInputs(logicalId).length == 0;
	}

	public boolean isSink(Integer logicalId)
	{
		return logicalId != null && getNextHopLogicalNodeId(logicalId) == null;
	}

	public boolean isOperator(Integer logicalId)
	{
		return logicalId != null && logicalTopology.containsKey(logicalId) && !isSource(logicalId) && !isSink(logicalId);
	}

	public Set<Integer> getSourcePhysicalIds()
	{
		Set<Integer> result = new HashSet<Integer>();
		Iterator iter = log2phys.keySet().iterator();
		while (iter.hasNext())
		{
			Integer logicalId = (Integer)iter.next();
			if (isSource(logicalId))
			{
				result.addAll(getPhysicalNodeIds(logicalId));
			}
		}
		if (result.isEmpty()) { throw new RuntimeException("Logic error, getSourcePhysicalIds."); }
		return result;
	}

	public Integer getSinkPhysicalId()
	{
		Integer sinkLogicalId = getSinkLogicalId();
		Set physicalIds = getPhysicalNodeIds(sinkLogicalId);
		if (physicalIds.isEmpty() || physicalIds.size() > 1) { throw new RuntimeException("Logic error: size = "+physicalIds.size()); }
		return (Integer)(physicalIds.iterator().next());
	}

	public Set<Integer> getOpPhysicalIds()
	{
		Set<Integer> result = new HashSet<Integer>();
		Iterator iter = log2phys.keySet().iterator();
		while (iter.hasNext())
		{
			Integer logicalId = (Integer)iter.next();
			if (isOperator(logicalId))
			{
				result.addAll(getPhysicalNodeIds(logicalId));
			}
		}
		//Could have just a source and sink, so ok to be empty.
		//if (result.isEmpty()) { throw new RuntimeException("Logic error."); }
		return result;
	}

	public boolean isParallel(Integer logicalId)
	{
		return parallelized.contains(logicalId);
	}

	public boolean hasParallelism()
	{
		return !parallelized.isEmpty();
	}

	public int getHeight(Integer logicalId)
	{

		int sinkLogicalId = getSinkLogicalId();
		int queryHeight = getSubQueryHeight(sinkLogicalId, 0);

		int opBranchHeight = 0;
		int currentLogicalId = logicalId;
		while (currentLogicalId != sinkLogicalId)
		{
			currentLogicalId = getNextHopLogicalNodeId(currentLogicalId);
			opBranchHeight++;
		}	

		return queryHeight - opBranchHeight;
	}

	private int getSubQueryHeight(int logicalId, int currentHeight)
	{
		int maxHeight = currentHeight;
		Integer[] inputs = getLogicalInputs(logicalId);
		if (inputs.length > 0)
		{
			for (int i = 0; i < inputs.length; i++)
			{
				maxHeight = Math.max(maxHeight, getSubQueryHeight(inputs[i], maxHeight +1));
			}
		}
		return maxHeight;
	}

	public Integer getNextHopLogicalNodeId(Integer logicalId)
	{
		if (!logicalTopology.keySet().contains(logicalId)) throw new RuntimeException("Invalid logical node id.");
		Iterator logIter = logicalTopology.keySet().iterator();
		while (logIter.hasNext())
		{
			Integer nextId = (Integer)logIter.next();
			if (nextId.intValue() != logicalId.intValue())
			{
				Integer[] inputs = (Integer[])logicalTopology.get(nextId);
				for (int i = 0; i < inputs.length; i++)
				{
					if (inputs[i].intValue() == logicalId.intValue())
					{
						//i.e. the id of the next hop.
						return nextId;
					}
				}
			}
		}

		//No next hop -> logical id is a sink
		return null;
	}

	public TreeSet getJoinOpLogicalNodeIds()
	{
		TreeSet result = new TreeSet();
		Iterator iter = logicalTopology.keySet().iterator();
		while (iter.hasNext())
		{
			Integer opId = (Integer)iter.next();
			if (((Integer[])logicalTopology.get(opId)).length > 1)
			{
				result.add(opId);
			}
		}

		return result;
	}

	public boolean hasReplication()
	{
		Iterator iter = logicalTopology.keySet().iterator();
		while (iter.hasNext())
		{
			if (((Set)log2phys.get(iter.next())).size() > 1)
			{
				return true;
			}
		}
		return false;
	}

	private String logicalTopologyToString()
	{
		String result = "";
		Iterator iter = logicalTopology.keySet().iterator();
		while (iter.hasNext())
		{
			Integer parent = (Integer)iter.next();
			Integer[] logicalInputs = getLogicalInputs(parent);
			String nodeResult = parent + " <- {";
			for (int i = 0; i < logicalInputs.length; i++)
			{
				nodeResult += logicalInputs[i].intValue() + (i+1 < logicalInputs.length ? ",":"");
			}

			result += nodeResult + "}"+(iter.hasNext() ? ", ":"");
		}

		return result;
	}

	private String log2physToString()
	{
		String result = "";
		Iterator logIter = log2phys.keySet().iterator();
		while (logIter.hasNext())
		{
			Integer logicalId = (Integer)logIter.next();
			Set physicalIds = getPhysicalNodeIds(logicalId);
			Iterator physIter = physicalIds.iterator();
			String nodeResult = logicalId + " -> {";
			while (physIter.hasNext())
			{
				Integer physicalId = (Integer)physIter.next();
				nodeResult += physicalId + (physIter.hasNext() ? ",":"");
			}
			result += nodeResult + "}" + (logIter.hasNext() ? ", ":"");
		}
		return result;
	}

	//Return List<Set<physId>>?
	public List getAllPlans()
	{
		Integer logicalId = getSinkLogicalId();

		List allPlans = getLogicalNodeSubPlans(logicalId);

		if (logger.isDebugEnabled())
		{
			Iterator iter = allPlans.iterator();
			while (iter.hasNext()) { validatePlan((Set)iter.next()); }
		}

		return allPlans;
	}

	private Integer getSinkLogicalId()
	{
		Iterator iter = getLogicalNodeIds().iterator();
		while (iter.hasNext())
		{
			Integer logicalId = (Integer)iter.next();
			if (isSink(logicalId)) { return logicalId; }
		}
		throw new RuntimeException("Logic error, getSinkLogicalId");
	}

	private List getLogicalNodeSubPlans(Integer logicalId)
	{
		List result;

		if (isSource(logicalId))
		{
			Set source = new HashSet();
			result = new LinkedList();
			result.add(source);
			result = addNewLogicalIdToPlans(logicalId, result);
		}
		else
		{
			List subPlans;
			if (getLogicalInputs(logicalId).length == 1)
			{
				subPlans = getLogicalNodeSubPlans(getLogicalInputs(logicalId)[0]);
			}
			else
			{
				Integer[] logicalInputs = getLogicalInputs(logicalId);
				if (logicalInputs.length != 2) { throw new RuntimeException("Logic error."); }

				List leftSubPlans = getLogicalNodeSubPlans(logicalInputs[0]);
				List rightSubPlans = getLogicalNodeSubPlans(logicalInputs[1]);

				subPlans = crossProduct(leftSubPlans, rightSubPlans);
			}

			result = addNewLogicalIdToPlans(logicalId, subPlans);
		}
		return result;
	}

	/** Expects a single empty plan if logical id is a source. */
	private List addNewLogicalIdToPlans(Integer logicalId, List plans)
	{
		if (isSource(logicalId) && !((Set)plans.get(0)).isEmpty()) { throw new RuntimeException("Logic error."); }
		Set physicalIds = getPhysicalNodeIds(logicalId);
		Iterator iter = physicalIds.iterator();
		List result = new LinkedList();

		while(iter.hasNext())
		{
			result.addAll(addNewPhysicalIdToPlans((Integer)iter.next(), clonePlans(plans)));
		}
		return result;
	}

	private List addNewPhysicalIdToPlans(Integer physicalId, List plans)
	{
		Iterator iter = plans.iterator();
		while (iter.hasNext())
		{
			Set plan = (Set)iter.next();
			if (plan.contains(physicalId)) { throw new RuntimeException("Plan already has physical id="+physicalId.intValue()+",plan="+plan); }
			plan.add(physicalId);
		}
		return plans;
	}

	private List clonePlans(List plans)
	{
		List clonedPlans = new LinkedList();
		Iterator iter = plans.iterator();
		while (iter.hasNext())
		{
			Set plan = (Set)iter.next();
			Set clone = new HashSet(plan);
			clonedPlans.add(clone);
		}
		return clonedPlans;
	}

	private List crossProduct(List leftPlans, List rightPlans)
	{
		List result = new LinkedList();

		Iterator leftIter = leftPlans.iterator();
		while (leftIter.hasNext())
		{
			Iterator rightIter = rightPlans.iterator();
			Set leftPlan = (Set)leftIter.next();
			while (rightIter.hasNext())
			{
				Set rightPlan = (Set)rightIter.next();
				Set product = new HashSet();
				product.addAll(leftPlan);
				product.addAll(rightPlan);
				if (product.size() != leftPlan.size() + rightPlan.size())
				{ throw new RuntimeException("Unexpected product plan size="+product.size()+",product="+product+",left="+leftPlan+",right="+rightPlan); }
				result.add(product);
			}
		}
		if (result.size() != leftPlans.size() * rightPlans.size()) { throw new RuntimeException("Logic error."); }
		return result;
	}

	public Integer getPlanNextHop(Integer physicalId, Set plan)
	{
		if (logger.isDebugEnabled()) { validatePlan(plan); }

		Integer logicalId = getLogicalNodeId(physicalId);
		if (isSink(logicalId)) { return null; }
		Integer nextHopLogicalId = getNextHopLogicalNodeId(logicalId);
		Set nextHopPhysicalIds = getPhysicalNodeIds(nextHopLogicalId);

		Integer result = null;
		Iterator physIter = nextHopPhysicalIds.iterator();
		while (physIter.hasNext())
		{
			Integer nextHopPhysicalId = (Integer)physIter.next();
			if (plan.contains(nextHopPhysicalId))
			{
				if (result == null) { result = nextHopPhysicalId; }
				else { throw new RuntimeException("Logic error."); }
			}
		}

		if (result != null) { return result; }
		else { throw new RuntimeException("Logic error."); }
	}

	private void validatePlan(Set plan)
	{
		logger.debug("Validating plan="+plan);
		Iterator iter = plan.iterator();
		Set planLogicalIds = new HashSet();
		while (iter.hasNext())
		{
			planLogicalIds.add(getLogicalNodeId((Integer)iter.next()));
		}

		if (plan.size() != getLogicalNodeIds().size() ||
				plan.size() != planLogicalIds.size()) { throw new RuntimeException("Logic error."); }

		iter = getLogicalNodeIds().iterator();
		while (iter.hasNext())
		{
			if (!planLogicalIds.contains(iter.next())) { throw new RuntimeException("Logic error.");}
		}
	}

	public Integer addrToNodeId(InetAddress addr)
	{
		Set<Integer> result = addr2phys.get(addr);
		if (!(result.size() == 1))
		{
			throw new RuntimeException("TODO: Won't work with two workers on the same node, need to include the port.");			
		}
		for (Integer id : result) { return id; }
		return null;
	}

	public int localSiblings(Integer physicalId)
	{
		int result = 1;
		Set<Integer> sameNodeWorkers = addr2phys.get(getNodeAddress(physicalId));
		if (sameNodeWorkers.size() > 1)
		{
			//Now need to count the number of replicas.
			Set siblings = getPhysicalNodeIds(getLogicalNodeId(physicalId));
			if (siblings.size() > 1)
			{
				//And finally all replicas on the same node.
				List<Integer> siblingsSameNode = new LinkedList<>();
				for (Integer snw : sameNodeWorkers)
				{
					if (siblings.contains(snw))
					{
						siblingsSameNode.add(snw);	
					}	
				}	
				result = siblingsSameNode.size();
			}
		}
		return result;

	}

	public int localSiblingIndex(Integer physicalId)
	{
		Set<Integer> sameNodeWorkers = addr2phys.get(getNodeAddress(physicalId));
		
		int localWorkerIndex = -1; //return -1 if the only replica on this ip

		if (sameNodeWorkers.size() > 1)
		{
			//Now need to count the number of replicas.
			Set siblings = getPhysicalNodeIds(getLogicalNodeId(physicalId));
			if (siblings.size() > 1)
			{
				//And finally all replicas on the same node.
				List<Integer> siblingsSameNode = new LinkedList<>();
				for (Integer snw : sameNodeWorkers)
				{
					if (siblings.contains(snw))
					{
						siblingsSameNode.add(snw);	
					}	
				}	

				//If more than one then return an index based on sort order (used to pick client port #'s).
				if (siblingsSameNode.size() > 1)
				{
					Collections.sort(siblingsSameNode);
					for (int i = 0; i < siblingsSameNode.size(); i++)
					{ 
						if (siblingsSameNode.get(i).equals(physicalId)) { localWorkerIndex = i; break; }
					}
					if (localWorkerIndex < 0) { throw new RuntimeException("Logic error for "+physicalId+" lwi="+localWorkerIndex); }
				}	
			}
		}
		
		logger.debug("Local worker index for "+ physicalId + ": "+localWorkerIndex);
		return localWorkerIndex;
	}	
}
