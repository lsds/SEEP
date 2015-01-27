package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class CostHandler implements Runnable
{
	private final Object lock = new Object(){};
	private final CoreRE owner;
	private final long RECOMPUTE_INTERVAL_MS = Integer.parseInt(GLOBALS.valueFor("routeRecomputeIntervalSec")) * 1000;
	private final long SWITCH_INTERVAL_MS = 5 * RECOMPUTE_INTERVAL_MS; //Temp hack until we interface with olsr
	private volatile boolean isShutdown = false;
	private long lastSwitchTime = -1;
	private int lastSwitch = 0;
	private int localOpId;

	public CostHandler(CoreRE owner)
	{
		this.owner = owner;
		this.localOpId = owner.getProcessingUnit().getOperator().getOperatorId();
	}


	public void run()
	{
		if (owner.getProcessingUnit().getOperator().getOpContext().getListOfDownstreamIndexes().isEmpty())
		{
			System.out.println("SNK (opid = "+localOpId + "): No need to have a router for sink node, cost handler returning.");
			return;
		}
		
		while (!isShutdown())
		{
			//TODO: Best way to get the potential targets etc?
			int newTarget = compute_lowestCostTarget();
			owner.getProcessingUnit().getOperator().getRouter().update_lowestCost(newTarget);
			long lastRecompute = System.currentTimeMillis();
			
			while (lastRecompute + RECOMPUTE_INTERVAL_MS > System.currentTimeMillis())
			{
				try {
					Thread.sleep(lastRecompute + RECOMPUTE_INTERVAL_MS - System.currentTimeMillis());
				} catch (InterruptedException e) {
					if (isShutdown()) { break; }
					
					e.printStackTrace();
				}
			}
		}
		System.out.println("Control handler thread exiting.");
	}
	
	public boolean isShutdown()
	{
		return isShutdown;
	}
	
	public void shutdown()
	{
		isShutdown = true;
	}
	
	private int compute_lowestCostTarget()
	{
		ArrayList<Integer> downstreamIndexes = owner.getProcessingUnit().getOperator().getOpContext().getListOfDownstreamIndexes();
		
		if (lastSwitchTime < 0)
		{
			lastSwitchTime = System.currentTimeMillis();
		}
		else if (lastSwitchTime + SWITCH_INTERVAL_MS <= System.currentTimeMillis())
		{
			lastSwitchTime = System.currentTimeMillis();
			lastSwitch++;
			lastSwitch = lastSwitch % downstreamIndexes.size();
		}
		
		System.out.println("Lowest cost dsIndex="+downstreamIndexes.get(lastSwitch));
		return downstreamIndexes.get(lastSwitch);
	}
}
