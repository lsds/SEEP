/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;

/** 
* ControlHandler. This class is in charge of managing control connections and attach them to a given thread that is in charge of serving them.
*/

public class ControlHandler implements Runnable{

	final private Logger LOG = LoggerFactory.getLogger(ControlHandler.class);
	
	//The core that owns this control handler
	private CoreRE owner;
	//The connection port that this controlhandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;
	private final Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues;
	private final BlockingQueue<Socket> ctrlConnQueue;
	private final boolean dummy;

	public CoreRE getOwner(){
		return owner;
	}
	
	public void setOwner(CoreRE owner){
		this.owner = owner;
	}

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public boolean getGoOn(){
		return goOn;
	}

	public void setGoOn(boolean goOn){
		this.goOn = goOn;
	}

	public ControlHandler(CoreRE owner, int connPort, Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues, BlockingQueue<Socket> ctrlConnQueue){
		this.owner = owner;
		this.connPort = connPort;
		this.goOn = true;
		this.ctrlQueues = ctrlQueues;
		this.ctrlConnQueue = ctrlConnQueue;
		this.dummy = false;
	}

	public ControlHandler(CoreRE owner, int connPort, Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues, BlockingQueue<Socket> ctrlConnQueue, boolean dummy){
		this.owner = owner;
		this.connPort = connPort;
		this.goOn = true;
		this.ctrlQueues = ctrlQueues;
		this.ctrlConnQueue = ctrlConnQueue;
		this.dummy = dummy;
	}
	
	public void run(){
		startCtrlQueueWorkers();
		if (ctrlConnQueue != null)
		{
			channelListen();	
		}
		else
		{
			socketListen();
		}
	}

	private void channelListen()
	{
		int socketCount = 0;
		//Piggybacking upstream control messages, so new conns received from ouptut queue worker.
		while(goOn){
			//Place new connections in a new thread. We have a thread per upstream connection
			Socket incomingConn = null;
			try
			{
				incomingConn = ctrlConnQueue.take();
			} catch(InterruptedException e) { LOG.error("Unexpected interrupt: "+e); System.exit(1); }
			String threadName = incomingConn.getInetAddress().toString();
			
			Thread newConn = new Thread(new ControlHandlerWorker(incomingConn, owner), "chw-"+threadName+"-T-"+socketCount++);
			newConn.start();
		}
	}

	private void socketListen()
	{
		int socketCount = 0;
		//Not piggybacking upstream control messages.
		ServerSocket controlServerSocket = null;
		try{
			//Establish listening port
			if (dummy) { controlServerSocket = new ServerSocket(connPort, 50, owner.getNodeDescr().getIp()); }
			else { controlServerSocket = new ServerSocket(connPort, 50, owner.getNodeDescr().getControlIp()); }
			controlServerSocket.setReuseAddress(true);
			LOG.info("-> ControlHandler listening on port: {}", connPort);
			//while goOn is active
			while(goOn){
				//Place new connections in a new thread. We have a thread per upstream connection
				Socket incomingConn = controlServerSocket.accept();
				String threadName = incomingConn.getInetAddress().toString();
				
				Thread newConn = new Thread(new ControlHandlerWorker(incomingConn, owner, dummy), "chw-"+threadName+"-T-"+socketCount++);
				newConn.start();
			}
			controlServerSocket.close();
		}
		catch(BindException be){
			LOG.error("-> BIND EXC IO Error "+be.getMessage());
			//LOG.error("-> controlServerSocket.toString: "+controlServerSocket.toString());
			be.printStackTrace();
		}
		catch(IOException io){
			LOG.error("-> ControlHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public void newIncomingConn(Socket conn)
	{
		if (ctrlConnQueue == null) { throw new RuntimeException("Logic error"); }
		try
		{
			ctrlConnQueue.put(conn);
		} catch(InterruptedException e) { LOG.error("Unexpected interrupt: "+e); System.exit(1); }
	}
 
	private void startCtrlQueueWorkers()
	{
		if (ctrlQueues == null) { return; }
		int socketCount = 0;
		for (Map.Entry<Integer, BlockingQueue<ControlTuple>> ctrlQueue : ctrlQueues.entrySet())
		{
			Thread newConn = new Thread(new ControlHandlerWorker(ctrlQueue.getValue(), owner), "chwq-"+ctrlQueue.getKey()+"-T-"+socketCount++);
			newConn.start();
		}
	}
}	
