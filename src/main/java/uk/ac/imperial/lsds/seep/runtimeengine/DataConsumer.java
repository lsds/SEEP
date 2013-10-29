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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

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
		Map<Integer, DataStructureI> inputDataModeMap = dataAdapter.getInputDataIngestionModeMap();
		// For performance reasons we make the differentiation between cases where more than 1 inputdataIngestion mode...
		if(inputDataModeMap.size() > 1){
			for(Entry<Integer, DataStructureI> entry : inputDataModeMap.entrySet()){
				DataConsumerWorker dcw = new DataConsumerWorker(entry.getValue());
				Thread worker = new Thread(dcw);
				worker.start();
			}
		}
		//... and only one, case that we can exploit for performance reasons
		else{
			DataStructureI dso = dataAdapter.getUniqueDso();
			if(dso instanceof InputQueue){
				while(doWork){
					DataTuple data = dso.pull();
					if(owner.checkSystemStatus()){
						owner.forwardData(data);
					}
				}
			}
			else if(dso instanceof Barrier){
				while(doWork){
					ArrayList<DataTuple> ldata = dso.pull_from_barrier();
					if(owner.checkSystemStatus()){
						owner.forwardData(ldata);
					}
				}
			}
		}
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
