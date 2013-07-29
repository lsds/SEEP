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
import java.util.Map;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.MetricsReader;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

/**
* IncomingDataHandler. This is in charge of managing incoming data connections and associate a thread to them
*/

public class IncomingDataHandler implements Runnable{

	//private Operator owner;
	private CoreRE owner;
	private int connPort;
	private boolean goOn;
	private Map<String, Integer> idxMapper;

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public IncomingDataHandler(CoreRE owner, int connPort, Map<String, Integer> idxMapper){
		this.owner = owner;
		this.connPort = connPort;
		//this.selector = initSelector();
		this.goOn = true;
		this.idxMapper = idxMapper;
	}

	public void run(){
		ServerSocket incDataServerSocket = null;
		try{
			//Establish listening port
			incDataServerSocket = new ServerSocket(connPort);
			incDataServerSocket.setReuseAddress(true);
			NodeManager.nLogger.info("-> IncomingDataHandler listening in port: "+connPort);
			//Upstream id
			int uid = 0;
			while(goOn){
				Thread newConn = new Thread(new IncomingDataHandlerWorker(uid, incDataServerSocket.accept(), owner, idxMapper));
				newConn.start();
				MetricsReader.numberIncomingDataHandlerWorkers.inc();
				uid++;
			}
			incDataServerSocket.close();
		}
		catch(BindException be){
			NodeManager.nLogger.severe("-> BIND EXC IO Error "+be.getMessage());
			NodeManager.nLogger.severe("-> Was trying to connect to: "+connPort);
			be.printStackTrace();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncomingDataHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}
