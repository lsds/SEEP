package uk.ac.imperial.lsds.seep.manet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class LinkCostHandler implements Runnable 
{
	private final Logger log = LoggerFactory.getLogger(LinkCostHandler.class);
	private final int linkCostPort = Integer.parseInt(GLOBALS.valueFor("linkCostPort"));
	private final CoreRE owner;
	private volatile boolean goOn = true;
	
	public LinkCostHandler(CoreRE owner)
	{
		this.owner = owner;
	}
	
	public void run()
	{
		ServerSocket serverSocket = null;
		LinkCostWorker worker = null;
		Thread workerT = null;
		
		// Start server socket in a loop listening on the link cost port.
		try{
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(owner.getNodeDescr().getIp(), linkCostPort));
		}
		catch(Exception e)
		{
			log.error("Could not bind to link cost server socket addr:"+owner.getNodeDescr().getIp()+":"+linkCostPort, e);
			System.exit(1);	//Temp.
		}
		
		while (goOn)
		{		
			
			try {
				//Spawn a worker to compute the lowest cost path to the destination,
				//notify the CORE EMANE link cost painter with the result, and			
				//update the local router with the lowest cost next hop.
				Socket incomingConn = serverSocket.accept();
				String threadName = incomingConn.getInetAddress().toString();
				log.info(owner.getNodeDescr().getIp()+":"+owner.getNodeDescr().getOwnPort()+
						"-> LinkCostHandler starting a new LinkCostWorker ");
				worker = new LinkCostWorker(owner, incomingConn);
				workerT = new Thread(worker, "lcw-"+threadName+"-T");
				workerT.start();
			} catch (IOException e) {
				log.error("Problem listening for link state updates on"+owner.getNodeDescr().getIp()+":"+linkCostPort, e);
				System.exit(1); //Temp
			}			
		}	
		log.info("xxxxxxxxx link cost handler serversocket closed xxxxxxxxxx");
		worker.setGoOn(false);
		workerT.interrupt();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setGoOn(boolean goOn) { this.goOn = goOn; }
}
