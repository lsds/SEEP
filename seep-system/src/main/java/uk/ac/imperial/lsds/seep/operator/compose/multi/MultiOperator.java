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
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public class MultiOperator implements StatelessOperator {

	private static final int SUB_QUERY_QUEUE_CAPACITY = Integer.valueOf(GLOBALS.valueFor("subQueryQueueCapacity"));
	private static final int SUB_QUERY_TRIGGER_DELAY = Integer.valueOf(GLOBALS.valueFor("subQueryTriggerDelay"));
//	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private API api;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;

	private ExecutorService executorService;
	
//	private Map<ISubQueryConnectable, Integer> numberThreadsPerSubQuery = new HashMap<>();
	
	private int subQueryTriggerCounter = 0;
	
	private MultiOperator(Set<ISubQueryConnectable> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
		for (ISubQueryConnectable c : this.subQueries)
			c.setParentMultiOperator(this);
	}
	
	@Override
	public void processData(DataTuple data, API api) {
		/*
		 * Store the api so that it can be later used to forward tuples
		 */
		this.api = api;
		
		/*
		 * If there is more than one most upstream operator, default 
		 * behaviour is that EVERY tuple is pushed to ALL input
		 * queues of these most upstream operators
		 */
		for (ISubQueryConnectable c : mostUpstreamSubQueries) {
			c.getSubQuery().pushData(data);
		}
		
		/*
		 * Determine whether the sub query queues should be checked in order
		 * to instantiate new sub query tasks
		 */
		if (this.subQueryTriggerCounter >= SUB_QUERY_TRIGGER_DELAY)
			checkSubQueryQueuesForTaskInstantiation();
		
		this.subQueryTriggerCounter++;
	}
	
	
	private Map<SubQuery, DataTuple> lastProcessedPointer = new HashMap<>();
	
	private void checkSubQueryQueuesForTaskInstantiation() {

		/*
		 * For each sub query
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			/*
			 * 
			 */
			
//			c.getSubQuery().execute(this.executorService, numberThreadsPerSubQuery.get(c), MICRO_OP_BATCH_SIZE);
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
				BlockingQueue<DataTuple> q = new ArrayBlockingQueue<DataTuple>(SUB_QUERY_QUEUE_CAPACITY);
				c.getSubQuery().registerOutputQueue(streamId, q);
				// register this queue as input of downstream 
				c.getLocalDownstream().get(streamId).getSubQuery().registerInputQueue(streamId, q);
			}
		}
		
		//TODO: think about better split up of number of threads
//		for (ISubQueryConnectable c : this.subQueries) 
//			numberThreadsPerSubQuery.put(c, new Double(Math.floor((numberOfCoresToUse*1f)/this.subQueries.size())).intValue());
		
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<ISubQueryConnectable> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
}
