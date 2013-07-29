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

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

/** 
* ControlHandler. This class is in charge of managing control connections and attach them to a given thread that is in charge of serving them.
*/

public class ControlHandler implements Runnable{

	//The core that owns this control handler
	private CoreRE owner;
	//The connection port that this controlhandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;

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

	public ControlHandler(CoreRE owner, int connPort){
		this.owner = owner;
		this.connPort = connPort;
		this.goOn = true;
	}
	
	public void run(){
		ServerSocket controlServerSocket = null;
		try{
			//Establish listening port
    		controlServerSocket = new ServerSocket(connPort);
			controlServerSocket.setReuseAddress(true);
			NodeManager.nLogger.info("-> ControlHandler listening in port: "+connPort);
			//while goOn is active
			while(goOn){
				//Place new connections in a new thread. We have a thread per upstream connection
				Thread newConn = new Thread(new ControlHandlerWorker(controlServerSocket.accept(), owner));
				newConn.start();
			}
			controlServerSocket.close();
		}
		catch(BindException be){
			NodeManager.nLogger.severe("-> BIND EXC IO Error "+be.getMessage());
			NodeManager.nLogger.severe("-> controlServerSocket.toString: "+controlServerSocket.toString());
			be.printStackTrace();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> ControlHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}	
