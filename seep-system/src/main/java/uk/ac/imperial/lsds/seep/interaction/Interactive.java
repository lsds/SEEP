/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Constantinos Vryonides - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.interactive;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.OperatorDeploymentException;
import uk.ac.imperial.lsds.seep.infrastructure.master.MasterController;

public class Interactive {

	final private static Logger LOG = LoggerFactory.getLogger(Interactive.class);

	public Interactive() {

	}

	//Execute as a master
	public void executeMaster(String[] args){
		//Get instance of MasterController and initialize it
		MasterController mc = MasterController.getInstance();
		mc.init();
		
		QueryPlan qp = null;
		//If the user provided a query when launching the master node...
		if(args[1] != null){
			if(!(args.length > 2)){
				System.out.println("Error. Main Master <path_to_query.jar> <Base_class_name>");
				System.exit(0);
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
			e.printStackTrace();
		}
	}
	
	//Execute as a worker
	public void executeSec(String args[]){
		//Read parameters from properties
		int port = Integer.parseInt(GLOBALS.valueFor("mainPort"));
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(GLOBALS.valueFor("mainAddr"));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int ownPort = 0;
		if(args.length > 2){
			System.out.println("Error. Main Worker <listen_port(optional)>");
			System.exit(0);
		}
		if(args.length > 1){
			ownPort = new Integer(args[1]);
		}
		else{
			ownPort = Integer.parseInt(GLOBALS.valueFor("ownPort"));
		}
		
		
		// NodeManager instantiation
		NodeManager nm = new NodeManager(port, bindAddr, ownPort);
		LOG.info("Initializing Node Manager...");
		nm.init();
		LOG.warn("NodeManager was remotely stopped");
	}

}