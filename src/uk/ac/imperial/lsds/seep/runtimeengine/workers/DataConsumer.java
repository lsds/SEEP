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
package uk.ac.imperial.lsds.seep.runtimeengine.workers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.runtimeengine.Barrier;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureI;
import uk.ac.imperial.lsds.seep.runtimeengine.InputQueue;

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
		Map<Integer, DataStructureI> inputDataModeMap = dataAdapter.getInputDataIngestionModeMap();
		for(Entry<Integer, DataStructureI> entry : inputDataModeMap.entrySet()){
			DataConsumerWorker dcw = new DataConsumerWorker(entry.getValue());
			Thread worker = new Thread(dcw);
			worker.start();
		}
//		if(dataAdapter.getDSO() instanceof InputQueue){
//			mode = 1;
//		}
//		else if(dataAdapter.getDSO() instanceof Barrier){
//			mode = 2;
//		}
//		if(mode == 1){
//			while(doWork){
//				DataTuple data = dataAdapter.pull();
//				if(owner.checkSystemStatus()){
//					owner.forwardData(data);
//				}
//			}
//		}
//		else if(mode == 2){
//			while(doWork){
////				System.out.println("### Yes, im in mode2, so using the barrier...");
//				ArrayList<DataTuple> ldata = dataAdapter.pull_from_barrier();
////				System.out.println("### Unblocked, got the data");
//				System.out.println("C");
//				if(owner.checkSystemStatus()){
//					System.out.println("D");
//					owner.forwardData(ldata);
//				}
//			}
//		}
	}
	
	class DataConsumerWorker implements Runnable{
		
		private DataStructureI dsi = null;
		
		public DataConsumerWorker(DataStructureI dsi){
			this.dsi = dsi;
		}

		@Override
		public void run() {
			if(dsi instanceof InputQueue){
				while(doWork){
					DataTuple data = dsi.pull();
					if(owner.checkSystemStatus()){
						owner.forwardData(data);
					}
				}
			}
			else if(dsi instanceof Barrier){
				while(doWork){
					ArrayList<DataTuple> ldata = dsi.pull_from_barrier();
					if(owner.checkSystemStatus()){
						owner.forwardData(ldata);
					}
				}
			}	
		}
	}
}
