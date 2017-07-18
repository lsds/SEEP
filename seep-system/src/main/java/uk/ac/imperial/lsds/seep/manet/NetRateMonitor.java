package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class NetRateMonitor implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(NetRateMonitor.class);
	private final static long NET_MONITOR_DELAY = 2 * 1000;
	private final Object lock = new Object(){};
	private final RoutingController rController;
	private final UpstreamRoutingController upstreamRoutingController;
	private Map <Integer, String> upOpIds;
	private final boolean coreDeployment;
	private final boolean piAdHocDeployment = "true".equals(GLOBALS.valueFor("piAdHocDeployment"));

	public NetRateMonitor(Map<Integer, String> upOpIds)
	{
		this.upOpIds = upOpIds;
		this.rController = null;
		this.upstreamRoutingController = null;
		coreDeployment = "true".equals(GLOBALS.valueFor("useCoreAddr"));
		logger.info("Net rate monitor using up op id to addr mapping: "+upOpIds);
	}

	public NetRateMonitor(Map<Integer, String> upOpIds, RoutingController rController)
	{
		this.upOpIds = upOpIds;
		this.rController = rController;
		this.upstreamRoutingController = null;
		coreDeployment = "true".equals(GLOBALS.valueFor("useCoreAddr"));
		logger.info("Net rate monitor using up op id to addr mapping: "+upOpIds);
	}

	
	public NetRateMonitor(Map<Integer, String> upOpIds, UpstreamRoutingController upstreamRoutingController)
	{
		this.upOpIds = upOpIds;
		this.upstreamRoutingController = upstreamRoutingController;
		this.rController = null;
		coreDeployment = "true".equals(GLOBALS.valueFor("useCoreAddr"));
		logger.info("Net rate monitor using up op id to addr mapping: "+upOpIds);
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
				Map<Integer, Double> upstreamCosts = null;
				if (coreDeployment)
				{
					if (logger.isDebugEnabled())
					{
						logNetInfo(allNetInfo());
						logNetInfo(tcpRtoInfo());
					}
					
					List<String> routes = readRoutes();
					if (GLOBALS.valueFor("net-routing").equals("OLSRETX"))
					{
						upstreamCosts = parseOLSRETXRoutes(routes);
					}
					else if (GLOBALS.valueFor("net-routing").equals("OLSR"))
					{
						upstreamCosts = parseOLSRRoutes(routes); 
					}
					else if (GLOBALS.valueFor("net-routing").equals("OSPFv3MDR"))
					{
						upstreamCosts = parseOLSRRoutes(routes); 
					}
					else if (GLOBALS.valueFor("net-routing").equals("FixedRoutes"))
					{
						upstreamCosts = parseFixedRoutes(routes); 
					}
					else { throw new RuntimeException("Unknown routing alg: "+GLOBALS.valueFor("net-routing")); }
					//TODO: Add empty routes/costs?
				}
				else if (piAdHocDeployment)
				{
					logNetInfo(allNetInfo());
					logNetInfo(tcpRtoInfo());
					List<String> routes = readRoutes();
					logger.info("Parsing " + GLOBALS.valueFor("net-routing") + " routes");
					if (GLOBALS.valueFor("net-routing").equals("OLSRETX"))
					{
						upstreamCosts = parseOLSRETXRoutes(routes);
					}
					else { throw new RuntimeException("Unsupported routing alg for Pi ad-hoc deployment: "+GLOBALS.valueFor("net-routing")); }
				}
				else
				{
					upstreamCosts = new HashMap<Integer, Double>();
					for (Integer upOp : upOpIds.keySet())
					{
						upstreamCosts.put(upOp, 1.0); 
					}
				}
				
				if (upstreamCosts !=  null && rController != null)
				{
					rController.handleNetCostsUpdate(upstreamCosts);
				}
				if (upstreamCosts !=  null && upstreamRoutingController != null)
				{
					upstreamRoutingController.handleNetCostsUpdate(upstreamCosts);
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
		
		logger.info("Read routes: "+result);
		return result;
	}
	
	private Map<Integer, Double> parseOLSRETXRoutes(List<String> routes)
	{
		Map<Integer, Double> upstreamCosts = new HashMap<>();
		logger.info("Checking routes against upOpIds: "+upOpIds);
		for (String route : routes)
		{
			String[] splits = route.split(" ");
			//TODO: Convert hostname/ip addresses to op ids (or vice versa)
			Set<Integer> hostUpOpIds = getHostUpOpIds(splits[1]);
			if (hostUpOpIds != null)
			{
				for (Integer upOpId : hostUpOpIds)
				{
					upstreamCosts.put(upOpId, Double.parseDouble(splits[2]));
				}
			}
		}
		
		logger.info("Up op costs: "+ upstreamCosts);
		return upstreamCosts;
	}
	
	private Map<Integer, Double> parseOLSRRoutes(List<String> routes)
	{
		return parseOLSRETXRoutes(routes);
	}
	
	private Map<Integer, Double> parseFixedRoutes(List<String> routes)
	{
		return parseOLSRETXRoutes(routes);
	}

	//Find ids of all operators/workers located on a particular host.
	private Set<Integer> getHostUpOpIds(String hostname)
	{
		Set<Integer> result = new HashSet<>();
		for (Integer upOpId : upOpIds.keySet())
		{
			String upOpHostname = upOpIds.get(upOpId);
			String upOpIp = null;
			try
			{
				upOpIp = InetAddress.getByName(upOpHostname).getHostAddress();
			}
			catch(UnknownHostException e)
			{
				logger.error("Unknown host for upOpId: " +e);
			}
			if (upOpHostname.equals(hostname) || upOpIp != null && upOpIp.equals(hostname))
			{
				result.add(upOpId);
			}
		}
		return result;
	}
	
	private List<String> allNetInfo()
	{
		String cmd = "./all-net-info.sh";
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
	
	private List<String> tcpRtoInfo()
	{
		String cmd = "./tcp-rto-info.sh";
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

	private void logNetInfo(List<String> netInfo)
	{
		for (String line : netInfo)
		{
			logger.debug(line);
		}
	}
	
	
	
}
