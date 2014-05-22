/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class MultiOperator implements OperatorCode, ComposedOperator, API {

	private static final int MICRO_OP_QUEUE_CAPACITY = Integer.valueOf(GLOBALS.valueFor("microOpQueueCapacity"));
	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<LocalConnectable> subOperators;
	private DistributedApi dApi;
	private Set<LocalConnectable> mostUpstreamMicroOperators;

	private ExecutorService executorService;
	
	private MultiOperator(Set<LocalConnectable> subOperators, int multiOpId){
		this.id = multiOpId;
		this.subOperators = subOperators;
		for (LocalConnectable c : this.subOperators)
			c.setParentMultiOperator(this);
	}
	
	public void setApi(DistributedApi api){
		this.dApi = api;
	}
	
	

	/** Implementation of OperatorCode interface **/

	@Override
	public void processData(DataTuple data, API localApi) {
		/*
		 * If there is more than one most upstream operator, default 
		 * behaviour is that EVERY tuple is pushed to ALL input
		 * queues of these most upstream operators
		 */
		for (LocalConnectable c : mostUpstreamMicroOperators) {
			c.getMicroOperator().pushData(data);
		}
	}
	
	@Override
	public void processData(List<DataTuple> dataList, API localApi) {
		for (DataTuple tuple : dataList)
			this.processData(tuple, localApi);
	}

	@Override
	public void setUp() {
		
		/*
		 * Create the thread pool 
		 */
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		//TODO: think about tuning this selection
		int numberOfCoresToUse = Math.max(numberOfCores, subOperators.size());
		
		this.executorService = Executors.newFixedThreadPool(numberOfCoresToUse);

		/*
		 * Identify most upstream and most downstream local operators
		 */
		this.mostUpstreamMicroOperators = new HashSet<>();
		for (LocalConnectable connectable : subOperators){
			if (connectable.isMostLocalUpstream())
				this.mostUpstreamMicroOperators.add(connectable);
		}
		
		/*
		 * Create graph of queues starting with most upstream
		 */
		for (LocalConnectable c : this.subOperators) {
			for (Integer streamId : c.getLocalDownstream().keySet()) {
				// create output queue
				BlockingQueue<DataTuple> q = new ArrayBlockingQueue<DataTuple>(MICRO_OP_QUEUE_CAPACITY);
				c.getMicroOperator().registerOutputQueue(streamId, q);
				// register this queue as input of downstream 
				c.getLocalDownstream().get(streamId).getMicroOperator().registerInputQueue(streamId, q);
			}
		}
		
		//TODO: think about better split up of number of threads
		Map<LocalConnectable, Integer> numberThreadsPerMicroOperator = new HashMap<>();
		for (LocalConnectable c : this.subOperators) 
			numberThreadsPerMicroOperator.put(c, new Double(Math.floor((numberOfCoresToUse*1f)/this.subOperators.size())).intValue());
		
		/*
		 * And "go" for the micro operators
		 */
		for (LocalConnectable c : this.subOperators) 
			c.getMicroOperator().execute(this.executorService, numberThreadsPerMicroOperator.get(c), MICRO_OP_BATCH_SIZE);
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<LocalConnectable> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
	@Override
	public int getNumberOfSubOperators() {
		return this.subOperators.size();
	}

	@Override
	public boolean isComposedOperatorStateful() {
		for (LocalConnectable c : this.subOperators)
			if (c.getMicroOperator() instanceof StatefulMicroOperator)
				return true;
		
		return false;
	}

	@Override
	public void send(DataTuple dt) {
		dApi.send(dt);
	}
	
	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		dApi.send_toStreamId(dt, streamId);
	}

	@Override
	public void send_all(DataTuple dt) {
		
	}

	@Override
	public void send_splitKey(DataTuple dt, int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toIndex(DataTuple dt, int idx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toStreamId_toAll(DataTuple dt, int streamId) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
        
    }

    @Override
    public void send_all_threadPool(DataTuple dt) {
        
    }

    @Override
    public void send_to_OpId(DataTuple dt, int opId) {
        
    }

    @Override
    public void send_to_OpIds(DataTuple[] dt, int[] opId) {
        
    }

    @Override
    public void send_toIndices(DataTuple[] dts, int[] indices) {
        
    }

	@Override
	public void setCallbackObject(Callback c) {
	}

}
