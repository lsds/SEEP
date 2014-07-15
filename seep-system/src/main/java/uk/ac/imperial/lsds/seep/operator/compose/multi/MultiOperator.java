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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class MultiOperator implements StatelessOperator {

	final private Logger LOG = LoggerFactory.getLogger(MultiOperator.class);
	
//	private static final int SUB_QUERY_TRIGGER_DELAY = Integer.valueOf(GLOBALS.valueFor("subQueryTriggerDelay"));
//	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private API api;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;
	private Set<ISubQueryConnectable> mostDownstreamSubQueries;
	
	private ExecutorService executorService;
	
//	private Map<ISubQueryConnectable, Integer> numberThreadsPerSubQuery = new HashMap<>();
 	
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
		 * Try to push to all input buffers of the most upstream sub queries
		 */
		for (ISubQueryConnectable q : this.mostUpstreamSubQueries) {
			for (ISubQueryBufferHandler handler : q.getLocalUpstreamBufferHandlers()) {
				// this code is accessed by a single thread only
//				synchronized (handler.getBuffer()) {
				while (!handler.getBuffer().add(data)) {
					try {
						handler.getBuffer().wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
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
		 * and create input/output buffers for them
		 */
		this.mostUpstreamSubQueries = new HashSet<>();
		this.mostDownstreamSubQueries = new HashSet<>();
		for (ISubQueryConnectable connectable : subQueries){
			if (connectable.isMostLocalUpstream()) {
				this.mostUpstreamSubQueries.add(connectable);
				
				UpstreamSubQueryBufferHandler upstreamHandler = new UpstreamSubQueryBufferHandler(connectable);
				connectable.addLocalUpstreamBufferHandler(upstreamHandler);
			}
			if (connectable.isMostLocalDownstream()) {
				this.mostDownstreamSubQueries.add(connectable);
				
				DownstreamSubQueryBufferHandler downstreamHandler = new DownstreamSubQueryBufferHandler(connectable);
				connectable.addLocalDownstreamBufferHandler(downstreamHandler);
			}
		}
		
		/*
		 * Start handlers for buffers between subqueries
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			for (Runnable r : c.getLocalDownstreamBufferHandlers())
				 (new Thread(r)).start();
		}

		/*
		 * Start handlers for input buffers of most upstream sub queries
		 */
		for (ISubQueryConnectable c : this.mostUpstreamSubQueries) {
			for (Runnable r : c.getLocalUpstreamBufferHandlers())
				 (new Thread(r)).start();
		}
		
		/*
		 * Start handlers for output buffers of most downstream sub queries
		 */
		for (ISubQueryConnectable c : this.mostDownstreamSubQueries) {
			for (Runnable r : c.getLocalDownstreamBufferHandlers())
				 (new Thread(r)).start();
		}

		
	}

	public API getAPI() {
		return this.api;
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
