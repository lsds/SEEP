package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class LinkCostHandler implements Runnable 
{
	private final Logger log = LoggerFactory.getLogger(LinkCostHandler.class);
	private final String linkCostMonitorAddr = GLOBALS.valueFor("linkCostMonitorAddr");
	private final int linkCostMonitorPort = Integer.parseInt(GLOBALS.valueFor("linkCostMonitorPort"));
	private final String linkPainterAddr = GLOBALS.valueFor("linkPainterAddr");
	private final int linkPainterPort = Integer.parseInt(GLOBALS.valueFor("linkPainterPort"));
	private final String charSet = GLOBALS.valueFor("linkCostCharSet");
	private final int RECOMPUTE_INTERVAL_MS = 5 * 1000;
	private final CoreRE owner;
	private volatile boolean goOn = true;
	private final NetworkAwareRouter netRouter;
	private final int localPhysicalId;
	
	public LinkCostHandler(CoreRE owner)
	{
		this.owner = owner;
		this.netRouter = new NetworkAwareRouter(
				owner.getProcessingUnit().getOperator().getOperatorId(),
				owner.getProcessingUnit().getOperator().getQuery());
		this.localPhysicalId = owner.getProcessingUnit().getOperator().getOperatorId();
	}
	
	public void run()
	{
		while(goOn)
		{
			String linkState = requestRoutingStateSync();
			log.error("Current routing state: "+ linkState);
			//TODO: Compute best target
			int newOperatorId = computeLowestCostOperatorId(linkState);
			if (newOperatorId != NetworkAwareRouter.NO_ROUTE && newOperatorId != localPhysicalId)
			{				
				int downOpIndex = owner.getProcessingUnit().getOperator().getOpContext().getDownOpIndexFromOpId(newOperatorId);
				log.info("Op "+localPhysicalId+" updating router to use new downstream operator "+newOperatorId+", target index="+downOpIndex);
				owner.getProcessingUnit().getOperator().getRouter().updateLowestCost(downOpIndex);
				//TODO: Update CORE routing graphics
				//updateCOREGraphicsSync(newTarget);
			}
			
			long lastRecompute = System.currentTimeMillis();
			
			while (lastRecompute + RECOMPUTE_INTERVAL_MS > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(lastRecompute + RECOMPUTE_INTERVAL_MS - System.currentTimeMillis());					
				} catch (InterruptedException e)
				{
					if (!goOn) { break; }
					log.error("Link cost thread interrupted.", e);
				}
			}
		}
		log.warn("Link cost handler thread exiting.");
	}
	
	
	public void setGoOn(boolean goOn) { this.goOn = goOn; }
	private String requestRoutingStateSync() 
	{ 
		//Open a connection to the link cost monitor
		Socket s = null;	
		String result = "";
		
		//TODO: Remove sys exits once stabilized
		try
		{			
			try {
				s = new Socket(linkCostMonitorAddr, linkCostMonitorPort);
			} catch (UnknownHostException e) {
				log.error("Error opening link cost monitor socket: ",e); System.exit(1);
			} catch (IOException e) {
				log.error("Error opening link cost monitor socket: ",e); System.exit(1);
			}
			
			//Read link cost update
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(s.getInputStream(), charSet));
			} catch (UnsupportedEncodingException e) {
				log.error("Error opening link cost reader: ",e); System.exit(1);
			} catch (IOException e) {
				log.error("Error opening link cost reader: ",e); System.exit(1);
			}
	
			//Read until EOF
			while (goOn)
			{
				try {
					String line = reader.readLine();
					if (line != null)
					{
						log.info("Link cost handler read line:"+ line);
						result += line;
					}
					else
					{
						break;
					}
				} catch (IOException e) {
					log.error("Error reading line from link cost monitor: ",e); System.exit(1);
				}
			}
		}
		finally
		{
			//Close connection
			try {
				s.close();
			} catch (IOException e) {
				log.warn("Error closing connection to link cost monitor.");
			}
		}

		return result;
	}
	
	private int computeLowestCostOperatorId(String linkState) 
	{ 		
		Map<Integer, Map<Integer, Integer>> netTopology = parseLinkState(linkState); 
		
		//int localOpId = owner.getProcessingUnit().getOperator().getOperatorId();
		//int localNodeId = owner.getProcessingUnit().getOperator().getOpContext().getOperatorStaticInformation().getMyNode().getNodeId();
		//InetAddress localNodeIp = owner.getProcessingUnit().getOperator().getOpContext().getOperatorStaticInformation().getMyNode().getIp();
		//int localNodePort = owner.getProcessingUnit().getOperator().getOpContext().getOperatorStaticInformation().getMyNode().getPort();
		//Query q = owner.getProcessingUnit().getOperator().getQuery();
		//log.info("Local logical id = "+q.getLogicalNodeId(localOpId));
		//int downOpId = owner.getProcessingUnit().getOperator().getOpContext().getDownOpIdFromIndex(index);

		/* In infrastructure
		ArrayList<Operator> ops = qp.getOps();
		src = qp.getSrc();
		snk = qp.getSnk();
		queryToNodesMapping = qp.getMapOperatorToNode();
		
		In Operator
		compose(Connectable down)
		opContext.addDownstream(down.getOperatorId());
		
		*/

		//GraphUtil.logTopology(netTopology);
		return netRouter.route(netTopology);
	} 
	
	private Map<Integer,Map<Integer, Integer>> parseLinkState(String linkState)
	{
		//src-id -> {dest-id -> cost}
		Map<Integer, Map<Integer, Integer>> result = new HashMap<Integer, Map<Integer, Integer>>();
		String trimmedBraces = linkState.substring(1, linkState.length() -1);
		if (!linkState.equals("{"+trimmedBraces+"}")) { throw new RuntimeException("Logic error: invalid link state string:"+linkState); }
		String[] splits = trimmedBraces.split("\\}, ");
		String lastSplit = splits[splits.length-1];
		splits[splits.length-1] = lastSplit.substring(0, lastSplit.length() -1);
		for (String split : splits)
		{
			//log.info("Split="+split);
			Integer src = Integer.parseInt(split.substring(0, split.indexOf(":")));
			result.put(src, new HashMap<Integer, Integer>());
			String destCostsUnsplit = split.substring(split.indexOf(":")+1, split.length());
			String[] destCostsSplits = destCostsUnsplit.split(",");
			destCostsSplits[0] = destCostsSplits[0].substring(2, destCostsSplits[0].length());

			for (String destCostSplit : destCostsSplits)
			{
				//log.info("DestCostSplit="+destCostSplit);
				 
				String[] destCost = destCostSplit.split(":");
				Integer dest = Integer.parseInt(destCost[0].trim());
				if (destCost.length > 2) { throw new RuntimeException("destCost length > 2: " + destCostSplit); }
				if (!"None".equals(destCost[1].trim()))
				{
					Integer cost = Integer.parseInt(destCost[1].trim());
					result.get(src).put(dest, cost);
				}				
			}			
		}
		return result;		
	}
	
	
	private void updateCOREGraphicsSync(int downstreamOpId) 
	{ 
		//Open a connection to the link cost monitor
		Socket s = null;
		
		//TODO: Remove sys exits once stabilized
		try
		{
			try {
				s = new Socket(linkPainterAddr, linkPainterPort);
			} catch (UnknownHostException e) {
				log.error("Error opening link painter socket: ",e); System.exit(1);
			} catch (IOException e) {
				log.error("Error opening link painter socket: ",e); System.exit(1);
			}
			
			//Read link cost update
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), charSet));
			} catch (UnsupportedEncodingException e) {
				log.error("Error opening link painter writer: ",e); System.exit(1);
			} catch (IOException e) {
				log.error("Error opening link painter writer: ",e); System.exit(1);
			}
	
			//TODO: Get ip of newTarget?
			try {				
				InetAddress downstreamIp = owner.getProcessingUnit().getOperator().getQuery().getNodeAddress(downstreamOpId); 
				int downstreamPort = owner.getProcessingUnit().getOperator().getOpContext().findDownstream(downstreamOpId).location().getMyNode().getPort();
				/*
				Integer downstreamNodeId = owner.getProcessingUnit().getOperator().getQuery().addrToNodeId(downstreamIp);
				Integer localOpId = owner.getProcessingUnit().getOperator().getOperatorId();
				InetAddress localIp = owner.getProcessingUnit().getOperator().getQuery().getNodeAddress(localOpId);
				Integer localNodeId = owner.getProcessingUnit().getOperator().getQuery().addrToNodeId(localIp);
				*/
				writer.write(owner.getNodeDescr().getIp()+":"+owner.getNodeDescr().getOwnPort() + ","+downstreamIp.getHostAddress()+":"+downstreamPort+"\n");
			} catch (IOException e) {
				log.error("Error writing target to painter", e); System.exit(1);
			}
		}
		finally
		{
			//Close connection
			try {
				s.close();
			} catch (IOException e) {
				log.warn("Error closing connection to link cost monitor.");
			}
		}
	}	
}
