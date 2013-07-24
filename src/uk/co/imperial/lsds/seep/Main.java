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
package uk.co.imperial.lsds.seep;

import java.net.InetAddress;
import java.net.UnknownHostException;

import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.infrastructure.OperatorDeploymentException;
import uk.co.imperial.lsds.seep.infrastructure.api.QueryPlan;
import uk.co.imperial.lsds.seep.infrastructure.master.MasterController;

/**
* Main. The entry point of the whole system. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
	
	//Runtime variable globals
	///\fixme{remove this shit}
	public static int eventR;
	public static int period;
	public static boolean maxRate;
	public static boolean eftMechanismEnabled;
	public static int numberOfXWays;
	
	//Properties variable
	static P p = new P();
	
	public static void main(String args[]){
		
		Main instance = new Main();
		//Load configuration properties from the config file
		
		p.loadProperties();
		
		if(args.length == 0){
			System.out.println("ARGS:");
			System.out.println("Master <querySourceFile.jar> <MainClass>");
			System.out.println("Worker <localPort>");
			System.exit(-1);
		}

		if(args[0].equals("Master")){
			instance.executeMaster(args);
		}
		else if(args[0].equals("Worker")){
			//secondary receives port and ip of master node
			instance.executeSec(args);
		}
		else{
			System.out.println("Error. See Usage");
			System.exit(-1);
		}
	}
	
	private void executeMaster(String[] args){
		//Get instance of MasterController and initialize it
		MasterController mc = MasterController.getInstance();
		mc.init();
		
		QueryPlan qp = null;
		//If the user provided a query when launching the master node...
		if(args[1] != null){
			if(!(args.length > 2)){
				System.out.println("Error. See Usage");
				System.exit(-1);
			}
			//Then we execute the compose method and get the QueryPlan back
			qp = mc.executeComposeFromQuery(args[1], args[2]);
			//Once we have the QueryPlan from the user submitted query, we submit the query plan to the MasterController
			mc.submitQuery(qp);
		}
		//In any case we start the MasterController to get access to the interface
		try {
			mc.start();
		} 
		catch (OperatorDeploymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void executeSec(String args[]){
		//Read parameters from properties
		int port = Integer.parseInt(P.valueFor("mainPort"));
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(P.valueFor("mainAddr"));
		} 
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int ownPort = 0;
		if(args.length > 1){
			ownPort = new Integer(args[1]);
		}
		else{
			ownPort = Integer.parseInt(P.valueFor("ownPort"));
		} 
		
		
		// NodeManager instantiation
		NodeManager nm = new NodeManager(port, bindAddr, ownPort);
		NodeManager.nLogger.info("Initializing Node Manager...");
		nm.init();
		NodeManager.nLogger.info("NodeManager was remotely stopped");
	}
}
