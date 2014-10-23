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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;

/**
* IncomingDataHandler. This is in charge of managing incoming data connections and associate a thread to them
*/

public class IncomingDataHandler implements Runnable{

	final private Logger LOG = LoggerFactory.getLogger(IncomingDataHandler.class);
	
	//private Operator owner;
	private CoreRE owner;
	private int connPort;
	private boolean goOn;
	private Map<String, Integer> idxMapper;
	private DataStructureAdapter dsa;

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public IncomingDataHandler(CoreRE owner, int connPort, Map<String, Integer> idxMapper, DataStructureAdapter dsa){
		this.owner = owner;
		this.connPort = connPort;
		//this.selector = initSelector();
		this.goOn = true;
		this.idxMapper = idxMapper;
		this.dsa = dsa;
	}

	public void run(){
		ServerSocket incDataServerSocket = null;
		try{
			//Establish listening port
			incDataServerSocket = new ServerSocket(connPort);
			incDataServerSocket.setReuseAddress(true);
			System.out.println("-> IncomingDataHandler listening in port: {}"+ connPort);
			//Upstream id
			while(goOn){
				Thread newConn = new Thread(new IncomingDataHandlerWorker(incDataServerSocket.accept(), owner, idxMapper, dsa));
				newConn.start();
			}
			incDataServerSocket.close();
		}
		catch(BindException be){
			LOG.error("-> BIND EXC IO Error "+be.getMessage());
			LOG.error("-> Was trying to connect to: "+connPort);
			be.printStackTrace();
		}
		catch(IOException io){
			LOG.error("-> IncomingDataHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}
