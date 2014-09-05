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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.interactive.Interactive;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.OperatorDeploymentException;
import uk.ac.imperial.lsds.seep.infrastructure.master.MasterController;

/**
* Main. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
		
	public static void main(String args[]){

		Interactive instance = null;
		
		if(args.length == 0){
			System.out.println("ARGS:");
			System.out.println("Master <querySourceFile.jar> <policyRulesFile.jar> <MainClass> <Interactive>/<Webserver>");
			System.out.println("Worker <localPort>");
			System.out.println("Worker <MasterIp> <localPort>");
			System.exit(0);
		}

		if(args[0].equals("Master")){
			if (args.length != 5) {
				exitInvalidArgs();
			}
			if (args[4].equals("Interactive")) {
				instance = new Interactive();
				String[] newArgs = Arrays.copyOfRange(args,0,3);
				instance.executeMaster(newArgs);
			}
			else if (args[4].equals("Webserver")){
				/* TODO JETTY STUFF */
			}
			else {
				exitInvalidArgs();
			}
		}
		else if(args[0].equals("Worker")){
			//secondary receives port and ip of master node§
			instance = new Interactive();
			instance.executeSec(args);
		}
		else{
			exitInvalidArgs();
		}
	}

	//Exit application because of invalid arguments
	private static void exitInvalidArgs() {
		System.out.println("Unrecognized command. Run again without arguments to view the required parameters.");
		System.exit(0);
	}
	
}
