/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.runtimeengine.workers;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.co.imperial.lsds.seep.runtimeengine.Barrier;
import uk.co.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.co.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.co.imperial.lsds.seep.runtimeengine.InputQueue;

public class DataConsumer implements Runnable {

	private CoreRE owner;
	private DataStructureAdapter dataAdapter;
	private boolean doWork = true;
	
	public void setDoWork(boolean doWork){
		this.doWork = doWork;
	}
	
	public DataConsumer(CoreRE owner, DataStructureAdapter dataAdapter){
		this.owner = owner;
		this.dataAdapter = dataAdapter;
	}

	@Override
	public void run() {
		int mode = 0;
		if(dataAdapter.getDSO() instanceof InputQueue){
			mode = 1;
		}
		else if(dataAdapter.getDSO() instanceof Barrier){
			mode = 2;
		}
		while(doWork){
			if(mode == 1){
				DataTuple data = dataAdapter.pull();
				if(owner.checkSystemStatus()){
					owner.forwardData(data);
				}
			}
			else if(mode == 2){
//				System.out.println("### Yes, im in mode2, so using the barrier...");
				ArrayList<DataTuple> ldata = dataAdapter.pullBarrier();
//				System.out.println("### Unblocked, got the data");
				System.out.println("C");
				if(owner.checkSystemStatus()){
					System.out.println("D");
					owner.forwardData(ldata);
				}
			}
		}
		System.out.println("DATA CONSUMER: IM DEAD");
		System.exit(0);
		
	}
}
