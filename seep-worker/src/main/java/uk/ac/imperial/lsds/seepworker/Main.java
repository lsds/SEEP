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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import joptsimple.OptionParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.comm.IOComm;
import uk.ac.imperial.lsds.seep.comm.serialization.JavaSerializer;
import uk.ac.imperial.lsds.seep.config.CommandLineArgs;
import uk.ac.imperial.lsds.seep.config.ConfigKey;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.infrastructure.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.util.Utils;
import uk.ac.imperial.lsds.seepworker.comm.WorkerMasterAPIImplementation;
import uk.ac.imperial.lsds.seepworker.comm.WorkerMasterCommManager;
import uk.ac.imperial.lsds.seepworker.infrastructure.NodeManager;


public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String args[]){
		
		// Get properties from command line
		List<ConfigKey> configKeys = WorkerConfig.getAllConfigKey();
		OptionParser parser = new OptionParser();
		CommandLineArgs cla = new CommandLineArgs(args, parser, configKeys);
		Properties commandLineProperties = cla.getProperties();
		
		// Get properties from file, if any
		Properties fileProperties = null;
		if(commandLineProperties.containsKey("properties.file")){
			String propertiesFile = commandLineProperties.getProperty("properties.file");
			fileProperties = Utils.readPropertiesFromFile(propertiesFile, false);
		}
		else{
			fileProperties = Utils.readPropertiesFromFile("config.properties", true);
		}
		
		Properties validatedProperties = Utils.overwriteSecondPropertiesWithFirst(commandLineProperties, fileProperties);
		//TODO: validate properties
		
		WorkerConfig wc = new WorkerConfig(validatedProperties);
		Main instance = new Main();
		instance.executeWorker(wc);
	}
	
	private void executeWorker(WorkerConfig wc){
		// TODO: Read parameters from properties
		int masterPort = wc.getInt(WorkerConfig.MASTER_PORT);
		InetAddress masterIp = null;
		try {
			masterIp = InetAddress.getByName(wc.getString(WorkerConfig.MASTER_IP));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Get connection to master node
		int masterId = Utils.computeIdFromIpAndPort(masterIp, masterPort);
		Connection masterConnection = new Connection(new EndPoint(masterId, masterIp, masterPort));
		
		int myPort = wc.getInt(WorkerConfig.LISTENING_PORT);
		
		// Create workerMaster comm manager
		Comm comm = new IOComm(new JavaSerializer(), Executors.newCachedThreadPool());
		WorkerMasterAPIImplementation api = new WorkerMasterAPIImplementation(comm, wc);
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[0], this.getClass().getClassLoader());
		
		WorkerMasterCommManager wmcm = new WorkerMasterCommManager(myPort, api, rcl);
		wmcm.start();
		
		// bootstrap
		String myIp = null;
		try {
			myIp = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("about to call bootstrap");
		api.bootstrap(masterConnection, myIp, myPort);
		
		// NodeManager instantiation
		//NodeManager nm = new NodeManager(masterPort, masterIp, ownPort);
		//LOG.info("Initializing Node Manager...");
		//nm.init();
		//LOG.warn("NodeManager was remotely stopped");
	}
}
