package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class HashRouter implements IRouter {
	private final static Logger logger = LoggerFactory.getLogger(HashRouter.class);
	private final OperatorContext opContext;	//TODO: Want to get rid of this dependency!
	private Integer lastRouted = null;
	private int switchCount = 0;
	private final Object lock = new Object(){};
	private final ArrayList<Integer> downOps;
	private final int height;
	private final int opId;
	
	
	
	public HashRouter(OperatorContext opContext) {
		this.opContext = opContext;
		this.downOps = opContext.getDownstreamOpIdList();
		Query frontierQuery = opContext.getFrontierQuery(); 
		this.opId = opContext.getOperatorStaticInformation().getOpId();
		int logicalId = frontierQuery.getLogicalNodeId(opId);
		this.height = frontierQuery.getHeight(logicalId);
	}
	
	@Override
	public ArrayList<Integer> route(long batchId) {
		synchronized(lock)
		{
			int index = (int)((batchId / Math.pow(downOps.size(), height)) % downOps.size());	
			Integer downOpId = downOps.get(index);
			/*
			if (height == 1)
			{
				if (opId == 0) { downOpId = -2; }
				else if (opId == 10) { downOpId = -190; }
				else if (opId == 11) { downOpId = -189; }
				else if (opId == 12) { downOpId = -188; }
				else { throw new RuntimeException("Logic error."); }
			}
			*/
			/*
			if (height == 1)
			{
				logger.info("Down ops = "+downOps);
				Random r = new Random();
				double randomValue = r.nextDouble();
				
				if (opId == 0)
				{
					//1 + 1.75 + 17.5 = 20.25
					//1 / 20.25 ~= 0.05
					if (randomValue <= 0.05) { downOpId = 1; }
					else if (randomValue <= 0.05 + (1.75 * 0.05)) { downOpId = 110; }
					else { downOpId = 111; }
					
				}
				else if (opId == 10)
				{
					//1 + 1.7 + + 29 = 31.7
					//1 / 31.7 ~= 0.03
					if (randomValue <= 0.03) { downOpId = 1; }
					else if (randomValue <= 0.03 + (1.7 * 0.03)) { downOpId = 110; }
					else { downOpId = 111; }
				}
				else if (opId == 11)
				{
					//1 + 1 + 12 = 14
					//1 / 14 = 0.07
					if (randomValue <= 0.07) { downOpId = 1; }
					else if (randomValue <= 0.14) { downOpId = 110; }
					else { downOpId = 111; }
				}
			}
			*/
			
			if (downOpId != lastRouted)
			{
				switchCount++;
				logger.info("Switched route from "+lastRouted + " to "+downOpId+" (switch cnt="+switchCount+")");
				lastRouted = downOpId;
			}

			if (downOpId != null)
			{
				ArrayList<Integer> targets = new ArrayList<>();
				targets.add(opContext.getDownOpIndexFromOpId(downOpId));
				return targets;
			}
		}
		//TODO: Unmatched;
		return null;		
	}

	@Override
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp) {
		throw new RuntimeException("TODO");
	}
	
	
	@Override
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState) {
		throw new RuntimeException("Logic error");		
	}
	
	public Set<Long> areConstrained(Set<Long> queued)
	{
		return null;
	}
	
	public void handleDownFailed(int downOpId)
	{
		//throw new RuntimeException("TODO"); 
	}

	public Map<Integer, Set<Long>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated)
	{
		throw new RuntimeException("Logic error."); 
	}
}
