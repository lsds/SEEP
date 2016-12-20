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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class DataConsumer implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(DataConsumer.class);
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
			logger.info("Multiple input data ingestion modes.");
			for(Entry<Integer, DataStructureI> entry : inputDataModeMap.entrySet()){
				DataConsumerWorker dcw = new DataConsumerWorker(entry.getValue());
				Thread worker = new Thread(dcw);
				worker.start();
			}
		}
		//... and only one, case that we can exploit for performance reasons
		else{
			logger.info("1 input data ingestion mode.");
			DataStructureI dso = dataAdapter.getUniqueDso();
			if(dso instanceof InputQueue || dso instanceof OutOfOrderInputQueue || dso instanceof OutOfOrderFairInputQueue){
				logger.info("Pulling from input queue");
				while(doWork){
					DataTuple data = dso.pull();
//					DataTuple[] dataBatch = ((InputQueue)dso).pullMiniBatch();
					if(owner.checkSystemStatus()){
						owner.forwardData(data);
//						for(int i = 0; i<dataBatch.length; i++){
//							DataTuple data = dataBatch[i];
//							if(data != null)
//								owner.forwardData(data);
//							else
//								break;
//						}
					}
				}
			}
			else if(dso instanceof Barrier || dso instanceof OutOfOrderBufferedBarrier || dso instanceof OutOfOrderFairBufferedBarrier){
				logger.info("Pulling from barrier");
				while(doWork){
					ArrayList<DataTuple> ldata = dso.pull_from_barrier();
					logger.debug("Pulled from barrier");
					if(owner.checkSystemStatus()){
						logger.debug("Forwarding data: "+ldata);
						owner.forwardData(ldata);
					}
					logger.debug("Next.");
				}
			}
			else
			{
				logger.error("Unknown dso type.");
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
			if(dsi instanceof InputQueue || dsi instanceof OutOfOrderInputQueue || dsi instanceof OutOfOrderFairInputQueue){
				logger.info("Pulling from input queue.");
				while(doWork){
					DataTuple data = dsi.pull();
					if(owner.checkSystemStatus()){
						owner.forwardData(data);
					}
				}
			}
			else if(dsi instanceof Barrier){
				logger.info("Pulling from barrier.");
				while(doWork){
					ArrayList<DataTuple> ldata = dsi.pull_from_barrier();
					if(owner.checkSystemStatus()){
						owner.forwardData(ldata);
					}
				}
			}
			else
			{
				logger.error("Unknown dso type.");
			}
		}
	}
}
