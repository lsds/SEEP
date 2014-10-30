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
package uk.ac.imperial.lsds.seepworker;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;
import uk.ac.imperial.lsds.seepworker.infrastructure.NodeManager;

/**
* Main. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String args[]){
		
		Main instance = new Main();
		
		if(args.length == 0){
			System.out.println("ARGS:");
			System.out.println("Worker <localPort>");
			System.exit(0);
		}

		instance.executeWorker(args);
		
//		if(args[0].equals("Master")){
//			//instance.executeMaster(args);
//		}
//		else if(args[0].equals("Worker")){
//			//secondary receives port and ip of master node
//			instance.executeSec(args);
//		}
//		else{
//			System.out.println("Unrecognized command. Type 'Master' or 'Worker' to see usage directions for each mode.");
//			System.exit(0);
//		}
	}
	
	private void executeWorker(String args[]){
		// TODO: Read parameters from properties
		int masterPort = Integer.parseInt(GLOBALS.valueFor("mainPort"));
		InetAddress masterIp = null;
		try {
			masterIp = InetAddress.getByName(GLOBALS.valueFor("mainAddr"));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Get connection to master node
		int masterId = Utils.computeIdFromIpAndPort(masterIp, masterPort);
		Connection masterConnection = new Connection(new EndPoint(masterId, masterIp, masterPort));
		
		// TODO: store execution unit type -> got from properties, just in case
		
		// TODO: get own port from properties
		int ownPort = 0;
		if(args.length > 1){
			System.out.println("Error. Main Worker <listen_port(optional)>");
			System.exit(0);
		}
		if(args.length > 0){
			ownPort = new Integer(args[1]);
		}
		else{
			ownPort = Integer.parseInt(GLOBALS.valueFor("ownPort"));
		}
		
		// NodeManager instantiation
		NodeManager nm = new NodeManager(masterPort, masterIp, ownPort);
		LOG.info("Initializing Node Manager...");
		nm.init();
		LOG.warn("NodeManager was remotely stopped");
	}
}
