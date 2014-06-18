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
package uk.ac.imperial.lsds.seep.operator.compose.multi;

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
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class MultiOperator implements StatelessOperator {

	private static final int MICRO_OP_QUEUE_CAPACITY = Integer.valueOf(GLOBALS.valueFor("microOpQueueCapacity"));
	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private DistributedApi dApi;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;

	private ExecutorService executorService;
	
	private MultiOperator(Set<ISubQueryConnectable> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
		for (ISubQueryConnectable c : this.subQueries)
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
		for (ISubQueryConnectable c : mostUpstreamSubQueries) {
			c.getSubQuery().pushData(data);
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
		int numberOfCoresToUse = Math.max(numberOfCores, subQueries.size());
		
		this.executorService = Executors.newFixedThreadPool(numberOfCoresToUse);

		/*
		 * Identify most upstream and most downstream local operators
		 */
		this.mostUpstreamSubQueries = new HashSet<>();
		for (ISubQueryConnectable connectable : subQueries){
			if (connectable.isMostLocalUpstream())
				this.mostUpstreamSubQueries.add(connectable);
		}
		
		/*
		 * Create graph of queues starting with most upstream
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			for (Integer streamId : c.getLocalDownstream().keySet()) {
				// create output queue
				BlockingQueue<DataTuple> q = new ArrayBlockingQueue<DataTuple>(MICRO_OP_QUEUE_CAPACITY);
				c.getSubQuery().registerOutputQueue(streamId, q);
				// register this queue as input of downstream 
				c.getLocalDownstream().get(streamId).getSubQuery().registerInputQueue(streamId, q);
			}
		}
		
		//TODO: think about better split up of number of threads
		Map<ISubQueryConnectable, Integer> numberThreadsPerMicroOperator = new HashMap<>();
		for (ISubQueryConnectable c : this.subQueries) 
			numberThreadsPerMicroOperator.put(c, new Double(Math.floor((numberOfCoresToUse*1f)/this.subQueries.size())).intValue());
		
		/*
		 * And "go" for the micro operators
		 */
		for (ISubQueryConnectable c : this.subQueries) 
			c.getSubQuery().execute(this.executorService, numberThreadsPerMicroOperator.get(c), MICRO_OP_BATCH_SIZE);
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<ISubQueryConnectable> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
}
