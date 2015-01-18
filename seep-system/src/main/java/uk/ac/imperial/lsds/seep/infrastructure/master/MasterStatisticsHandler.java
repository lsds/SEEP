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
package uk.ac.imperial.lsds.seep.infrastructure.master;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * MasterStatisticsHandler. This class is in the master node and is in charge of retrieving the performance times of the system
 */

public class MasterStatisticsHandler {
	private long crashStartTime = 0;
	private long parallelizationStartTime = 0;
	private int numOfUpstreams;
	private int msgReceived = 0;
	
	private boolean recovering = false;
	private boolean scalingOut = false;
	
	static public double th;
	static public int numberRunningMachines;
	
	//Indicate the initial point of initiation of recovery protocol
	public void setCrashInitialTime(long crashStartTime, int numOfUpstreams){
		this.crashStartTime = crashStartTime;
		this.numOfUpstreams = numOfUpstreams;
		//indicate that we are recovering a failure
		recovering = true;
	}

	//Indicate the initial point of scaling out protocol
	public void setParallelizationStartTime(long parallelizationStartTime, int numOfUpstreams){
		this.parallelizationStartTime = parallelizationStartTime;
		this.numOfUpstreams = numOfUpstreams;
		//indicate that we are scaling out an operator
		scalingOut = true;
	}

	//The system has finished scaling out or recovering
	public synchronized void setSystemStableTime(long systemStable){
		
		System.out.println("%%%%% "+(System.currentTimeMillis()-crashStartTime));
		
//		//new message received
//		msgReceived++;
//		double rate = ((double)(Main.eventR*Integer.parseInt(P.valueFor("sentenceSize"))/1000));
//		//compute time
//		//Once I have received all the required messages (from all the upstreams)
//System.out.println("RECV: "+msgReceived+" UPSize: "+numOfUpstreams);
//		if(msgReceived >= numOfUpstreams){
//			//If the system was recovering
//			if(recovering){
//				//All times are measured in the same machine
//				long recoveryTime = systemStable - crashStartTime;
//				crashStartTime = 0;
//				//reset the memory to process the following message
//				msgReceived = 0;
//				logData(rate, recoveryTime);
//			}
//			//If the system was scaling out
//			else if(scalingOut){
//				long parallelizationTime = systemStable - parallelizationStartTime;
//				parallelizationStartTime = 0;
//				//reset the memory to process the following message
//				msgReceived = 0;
//				logData(rate, parallelizationTime);
//			}
//		}
	}

	@Deprecated
	private void logData(double rate, long totalTime) {
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("tests/time.dat", true));
			if(scalingOut){
				scalingOut = false;
				System.out.println("PARALLELIZATION TIME: "+totalTime);
				//0 means parallelization
//				bw.write("0 "+Main.eventR+" "+totalTime+" "+numberRunningMachines);
			}
			else if(recovering){
				recovering = false;
				System.out.println("RECOVERY TIME: "+totalTime);
				//1 means fault
//				bw.write("1 "+Main.eventR+" "+totalTime+" "+numberRunningMachines);
			}
			bw.newLine();
			bw.close();
		}
		catch(IOException io){
			System.out.println("MonitorManager: While writing to file "+io.getMessage());
			io.printStackTrace();
		}
	}
}
