package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class NetRateMonitor implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(NetRateMonitor.class);
	private final static long NET_MONITOR_DELAY = 10 * 1000;
	private final Object lock = new Object(){};
	private final RoutingController rController;
	private ArrayList<Integer> upOpIds;
	private final boolean coreDeployment = false;
	public NetRateMonitor(ArrayList<Integer> upOpIds, RoutingController rController)
	{
		this.upOpIds = upOpIds;
		this.rController = rController;
	}
	
	private void setUpOpIds(ArrayList<Integer> newUpOpIds)
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
					String routes = readRoutes();
					upstreamCosts = parseRoutes(routes);
					//TODO: Add empty routes/costs?
				}
				else
				{
					for (Integer upOp : upOpIds)
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

	private String readRoutes()
	{
		String cmd = "route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep ' 1$' | sed \"s/^/`hostname` /\"";
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = null;
		StringBuilder result = new StringBuilder();
		try {
			process = pb.start();
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				result.append(line);
				result.append('\n');
			}
		} catch (IOException e) {
			logger.error("Error reading routes: "+e);
			System.exit(0);	//TODO: A bit harsh?
		}
		return result.toString();
	}
	
	private Map<Integer, Integer> parseRoutes(String routes)
	{
		throw new RuntimeException("TODO");
	}
}
