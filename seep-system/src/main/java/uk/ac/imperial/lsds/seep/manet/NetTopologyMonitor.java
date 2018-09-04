package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;

public class NetTopologyMonitor implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(NetTopologyMonitor.class);
	private final static long NET_MONITOR_DELAY = 2 * 1000;
	private final Object lock = new Object(){};
	
	private final Query frontierQuery;

	private final boolean coreDeployment;
	private final Router router;
	
	public NetTopologyMonitor(int localOpId, Query frontierQuery, Router router)
	{
		this.frontierQuery = frontierQuery;
		this.router = router;
		coreDeployment = "true".equals(GLOBALS.valueFor("useCoreAddr"));
	}
	
	/*
	private void setOpIds(Map<Integer, String> newUpOpIds, Map<Integer, String> newAllOpIds)
	{
		synchronized(lock) { 
			this.upOpIds = newUpOpIds; 
			this.allOpIds = newAllOpIds;
			lock.notifyAll();
		}
	}*/
		
	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized(lock)
		{
			while(true)
			{
				if (coreDeployment)
				{
					Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState = parseTopology(readTopology());
					router.getFrontierRouting().updateNetTopology(linkState);
					router.update_highestWeight(null);
					//upstreamCosts = parseRoutes(readRoutes());
					//TODO: Add empty routes/costs?
					//throw new RuntimeException("TODO"); 
				}
				else
				{
					throw new RuntimeException("TODO"); 
				}
				
				/*
				if (upstreamCosts !=  null)
				{
					rController.handleNetCostsUpdate(upstreamCosts);
				}
				*/
				
				try {
					Thread.sleep(NET_MONITOR_DELAY);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> parseTopology(List<String> links)
	{
		logger.info("Read links: "+links);
		Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState = new HashMap<>();
		
		for (String link : links)
		{
			String[] splits = link.split(" ");
			//TODO: Convert hostname/ip addresses to op ids (or vice versa)
			
			InetAddressNodeId srcAddr = null;
			InetAddressNodeId destAddr = null;
						
			try {
				srcAddr = new GraphUtil.InetAddressNodeId(InetAddress.getByName(splits[0]));
				destAddr = new InetAddressNodeId(InetAddress.getByName(splits[1]));
			} catch (UnknownHostException e) {
				logger.error("Exception parsing host ip: "+e);
				System.exit(1);
			}
			
			Double cost = GraphUtil.INFINITE_DISTANCE;
			if (!"INFINITE".equals(splits[2]))
			{
				cost = Double.parseDouble(splits[2]);
			}

			if (!linkState.containsKey(srcAddr))
			{
				linkState.put(srcAddr, new HashMap<InetAddressNodeId, Double>());
			}
			if (linkState.get(srcAddr).containsKey(destAddr))
			{
				Double existingCost = linkState.get(srcAddr).get(destAddr);
				if (! (existingCost.doubleValue() == cost.doubleValue()))
				{
					logger.error("TODO: Asymmetric links.");
					cost = Math.max(cost, existingCost);
				}
			}
			linkState.get(srcAddr).put(destAddr, cost);
			if (!linkState.containsKey(destAddr))
			{
				linkState.put(destAddr, new HashMap<InetAddressNodeId, Double>());
			}
			if (linkState.get(destAddr).containsKey(srcAddr))
			{
				Double existingCost = linkState.get(destAddr).get(srcAddr); 
				if (! (existingCost.doubleValue() == cost.doubleValue()))
				{
					logger.error("TODO: Asymmetric links.");
					cost = Math.max(cost, existingCost); 
				}
			}
			
			linkState.get(destAddr).put(srcAddr, cost);					
		}
		
		logger.info("Net link state: "+ linkState);
		return linkState;
	}
	
	private List<String> readTopology()
	{
		//String cmd = "route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep ' 1$' | sed \"s/^/`hostname` /\"";
		String cmd = "./net-topology.sh";
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", cmd);
		Process process = null;
		List<String> result = new LinkedList<>();
		try {
			process = pb.start();
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				result.add(line);
			}
		} catch (IOException e) {
			logger.error("Error reading topology: "+e);
			System.exit(0);	//TODO: A bit harsh?
		}
		return result;
	}
	
	//Return set as might want multiple workers on the same node.
	Set<Integer> getOpIds(String addr)
	{
		throw new RuntimeException("TODO"); 
	}
}
