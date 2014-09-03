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
package uk.ac.imperial.lsds.seep;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.android_seep_master.MainActivity;

import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.OperatorDeploymentException;
import uk.ac.imperial.lsds.seep.infrastructure.master.MasterController;

/**
* Main. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static MasterController mc;
	
	public void executeMaster(){
		//Get instance of MasterController and initialize it
		mc = MasterController.getInstance();
		mc.init();
	}
	
	public void deploy(String classname,int BaseInC, int BaseInD){
		QueryPlan qp = null;
		qp = mc.executeComposeFromQuery(classname);
		mc.submitQuery(qp, BaseInC,  BaseInD);

	}
	
	public void deploy0(int BaseInC, int BaseInD){
		mc.deployQueryToNodes( BaseInC,  BaseInD);
	}
	
	public void deploy1(){
		mc.deployQueryToNodes1();
	}
	
	public void deploy2(){
		mc.deployQueryToNodes2();

	}
	
	public void deploy3(){
		mc.deployQueryToNodes3();
	}
	
	public void plotRouting(){
		mc.plotRoutingMap();
	}
	
	public void start(){
		mc.startSystem();
	}
	
	public void stop(){
		mc.stopOperators();
	}
	
	public void executeSec(String args[]){
		//Read parameters from properties
	//	int port = Integer.parseInt(GLOBALS.valueFor("mainPort"));
		int port = 3500;
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(GLOBALS.valueFor("mainAddr"));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int ownPort = 0;
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
		//LOG.warn("NodeManager was remotely stopped");
	}
}
