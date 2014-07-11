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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class MultiOperator implements StatelessOperator {

	private static final int SUB_QUERY_TRIGGER_DELAY = Integer.valueOf(GLOBALS.valueFor("subQueryTriggerDelay"));
//	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private API api;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;
	private Set<ISubQueryConnectable> mostDownStreamSubQueries;
	
	private BlockingQueue<DataTuple> incomingTuples = new LinkedBlockingQueue<>(SubQueryBufferHandler.SUB_QUERY_QUEUE_CAPACITY);

	private ExecutorService executorService;
	
//	private Map<ISubQueryConnectable, Integer> numberThreadsPerSubQuery = new HashMap<>();
	
	private int subQueryTriggerCounter = 0;
 	
	private MultiOperator(Set<ISubQueryConnectable> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
		for (ISubQueryConnectable c : this.subQueries)
			c.setParentMultiOperator(this);
	}
	
	/**
	 * Note that pushing the data to the buffers of the most upstream 
	 * operators is not threadsafe. We assume a single thread to call 
	 * processData.
	 */
	@Override
	public void processData(DataTuple data, API api) {
		/*
		 * Store the api so that it can be later used to forward tuples
		 */
		this.api = api;
		
		/*
		 * Try to push to incoming queue from which the data items will
		 * be forwarded to the input buffers of the most upstream queries
		 */
		try {
			this.incomingTuples.put(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		/*
//		 * If there is more than one most upstream operator, default 
//		 * behaviour is that EVERY tuple is pushed to ALL input
//		 * queues of these most upstream operators
//		 */
//		for (ISubQueryConnectable c : mostUpstreamSubQueries) {
//			c.getSubQuery().pushDataToAllStreams(data);
//		}
//		
//		/*
//		 * Determine whether the sub query queues should be checked in order
//		 * to instantiate new sub query tasks
//		 */
//		if (this.subQueryTriggerCounter >= SUB_QUERY_TRIGGER_DELAY) {
//			checkForSubQueryTaskInstantiationAndTermination();
//			this.subQueryTriggerCounter = 0;
//		}
//		
//		this.subQueryTriggerCounter++;
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
		this.mostDownStreamSubQueries = new HashSet<>();
		for (ISubQueryConnectable connectable : subQueries){
			if (connectable.isMostLocalUpstream())
				this.mostUpstreamSubQueries.add(connectable);
			if (connectable.isMostLocalDownstream())
				this.mostDownStreamSubQueries.add(connectable);
		}
		
//		/*
//		 * Create graph of buffers and initialise buffer handlers.
//		 * Note that we create one buffer per pair of connected 
//		 * sub-queries. However, there may be multiple logical streams
//		 * defined between these subqueries, which will lead to 
//		 * different window batch definitions over the same buffer
//		 */
//		Set<Runnable> handlers = new HashSet<>();
//		/*
//		 *  for each sub query 
//		 */
//		for (ISubQueryConnectable c : this.subQueries) {
//			/*
//			 *  map that holds buffer for downstream query to avoid creating 
//			 *  multiple buffers in case of multiple logical streams between 
//			 *  the subqueries
//			 */
//			Map<ISubQueryConnectable, SubQueryBuffer> tmpDownstreamBuffers = new HashMap<>();
//			for (Integer downStreamId : c.getLocalDownstream().keySet()) {
//				ISubQueryConnectable down = c.getLocalDownstream().get(downStreamId);
//				// create buffer if not yet done
//				if (!tmpDownstreamBuffers.containsKey(down)) {
//					SubQueryBuffer q = new SubQueryBuffer(c, down, SUB_QUERY_QUEUE_CAPACITY);
//					
//					tmpDownstreamBuffers.put(down, q);
//					handlers.add(new SubQueryBufferHandler(this, q));
//				}
//				SubQueryBuffer q =  tmpDownstreamBuffers.get(down);
//				// register the buffer as output for the upstream
//				c.getSubQuery().registerOutputQueue(downStreamId, q);
//				// register this buffer as input of downstream 
//				down.getSubQuery().registerInputQueue(downStreamId, q);
//			}
//		}
		
		/*
		 * Start buffer handlers
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			for (Runnable r : c.getLocalDownstreamBufferHandlers())
				 (new Thread(r)).start();
		}
		
	}

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<ISubQueryConnectable> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}

	public ExecutorService getExecutorService() {
		return this.executorService;
	}
	
}
