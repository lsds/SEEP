package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class NetRateMonitor implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(NetRateMonitor.class);
	private final static long NET_MONITOR_DELAY = 2 * 1000;
	private final Object lock = new Object(){};
	private final RoutingController rController;
	private Map <Integer, String> upOpIds;
	private final boolean coreDeployment;
	public NetRateMonitor(Map<Integer, String> upOpIds, RoutingController rController)
	{
		this.upOpIds = upOpIds;
		this.rController = rController;
		coreDeployment = "true".equals(GLOBALS.valueFor("useCoreAddr"));
	}
	
	private void setUpOpIds(Map<Integer, String> newUpOpIds)
	{
		synchronized(lock) { 
			this.upOpIds = newUpOpIds; 
			lock.notifyAll();
		}
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized(lock)
		{
			while(true)
			{
				Map<Integer, Integer> upstreamCosts = null;
				if (coreDeployment)
				{
					upstreamCosts = parseRoutes(readRoutes());
					//TODO: Add empty routes/costs?
				}
				else
				{
					upstreamCosts = new HashMap<Integer, Integer>();
					for (Integer upOp : upOpIds.keySet())
					{
						upstreamCosts.put(upOp, 1); 
					}
				}
				
				if (upstreamCosts !=  null)
				{
					rController.handleNetCostsUpdate(upstreamCosts);
				}
				
				try {
					Thread.sleep(NET_MONITOR_DELAY);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private List<String> readRoutes()
	{
		//String cmd = "route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep ' 1$' | sed \"s/^/`hostname` /\"";
		String cmd = "./net-rates.sh";
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
			logger.error("Error reading routes: "+e);
			System.exit(0);	//TODO: A bit harsh?
		}
		return result;
	}
	
	private Map<Integer, Integer> parseRoutes(List<String> routes)
	{
		logger.info("Read routes: "+routes);
		Map<Integer, Integer> upstreamCosts = new HashMap<>();
		logger.info("Checking routes against upOpIds: "+upOpIds);
		for (String route : routes)
		{
			String[] splits = route.split(" ");
			//TODO: Convert hostname/ip addresses to op ids (or vice versa)
			Integer upOpId = getUpOpId(splits[1]);
			if (upOpId != null)
			{
				upstreamCosts.put(upOpId, Integer.parseInt(splits[2]));
			}
		}
		
		logger.info("Up op costs: "+ upstreamCosts);
		return upstreamCosts;
	}
	
	private Integer getUpOpId(String hostname)
	{
		for (Integer upOpId : upOpIds.keySet())
		{
			//TODO: What if two workers on the same node!
			if (upOpIds.get(upOpId).equals(hostname))
			{
				return upOpId;
			}
		}
		return null;
	}
}
