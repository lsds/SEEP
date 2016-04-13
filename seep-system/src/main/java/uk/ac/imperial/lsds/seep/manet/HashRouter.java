package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	
	
	public HashRouter(OperatorContext opContext) {
		this.opContext = opContext;
		this.downOps = opContext.getDownstreamOpIdList();
	}
	
	@Override
	public ArrayList<Integer> route(long batchId) {
		synchronized(lock)
		{
			int index = (int)(batchId % downOps.size());	
			Integer downOpId = downOps.get(index);
			
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
}
