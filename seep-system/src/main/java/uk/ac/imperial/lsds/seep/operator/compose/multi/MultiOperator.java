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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.WindowBatchTaskCreationScheme;

public class MultiOperator implements StatelessOperator {

	private static final int SUB_QUERY_QUEUE_CAPACITY = Integer.valueOf(GLOBALS.valueFor("subQueryQueueCapacity"));
	private static final int SUB_QUERY_TRIGGER_DELAY = Integer.valueOf(GLOBALS.valueFor("subQueryTriggerDelay"));
//	private static final int MICRO_OP_BATCH_SIZE = Integer.valueOf(GLOBALS.valueFor("microOpBatchSize"));
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private API api;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;
	private Set<ISubQueryConnectable> mostDownStreamSubQueries;

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
			c.getSubQuery().pushDataToAllStreams(data);
		}
		
		/*
		 * Determine whether the sub query queues should be checked in order
		 * to instantiate new sub query tasks
		 */
		if (this.subQueryTriggerCounter >= SUB_QUERY_TRIGGER_DELAY) {
			checkForSubQueryTaskInstantiationAndTermination();
			this.subQueryTriggerCounter = 0;
		}
		
		this.subQueryTriggerCounter++;
	}
	
	
	private Map<ISubQueryConnectable, DataTuple> lastStartedPointer = new HashMap<>();
	private Map<ISubQueryConnectable, Integer> lastFinishedOrderID = new HashMap<>();
	
	private SubQueryTaskCreationScheme creationScheme = new WindowBatchTaskCreationScheme();
	
	private Map<ISubQueryConnectable, List<SubQueryTask>> runningSubQueryTasks = new HashMap<>();
	
	private void checkForSubQueryTaskInstantiationAndTermination() {

		for (ISubQueryConnectable c : this.subQueries) {
			/*
			 * For each running task, check whether it has terminated
			 */
			Iterator<SubQueryTask> taskIter = runningSubQueryTasks.get(c).iterator();
			while (taskIter.hasNext()) {
				SubQueryTask task = taskIter.next();
				/*
				 * If computation is done
				 */
				if (task.isDone() && 
						(lastFinishedOrderID.get(task.getSubQueryConnectable()) == task.getLogicalOrderID() - 1)) {
					// Remove from running tasks
					taskIter.remove();
					// Get result
					try {
						List<DataTuple> result = task.get();
						// Update the respective queues with the result
						for (Entry<Integer, ISubQueryConnectable> downstreamEntry : task.getSubQueryConnectable().getLocalDownstream().entrySet()) {
							downstreamEntry.getValue().getSubQuery().pushData(result, downstreamEntry.getKey());
						}
						// Record progress by updating the last finished pointer
						lastFinishedOrderID.put(task.getSubQueryConnectable(), task.getLogicalOrderID());
					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
			}
		}
		
		/*
		 * For each sub query, check whether a task should be instantiated
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			/*
			 * Create tasks if there is enough data for starting the computation 
			 */
			creationScheme.init(c, lastStartedPointer.get(c));
			while (creationScheme.hasNext()) {
				SubQueryTask task = creationScheme.next();
				/*
				 * Submit the tasks
				 */
				executorService.execute(task);
				runningSubQueryTasks.get(c).add(task);
				lastStartedPointer.put(c, task.getLastProcessed());
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
		 */
		this.mostUpstreamSubQueries = new HashSet<>();
		this.mostDownStreamSubQueries = new HashSet<>();
		for (ISubQueryConnectable connectable : subQueries){
			if (connectable.isMostLocalUpstream())
				this.mostUpstreamSubQueries.add(connectable);
			if (connectable.isMostLocalDownstream())
				this.mostDownStreamSubQueries.add(connectable);
		}
		
		/*
		 * Create graph of queues
		 */
		for (ISubQueryConnectable c : this.subQueries) {
			for (ISubQueryConnectable down : c.getLocalDownstream().values()) {
				// create output queue
				BlockingDeque<DataTuple> q = new SubQueryBuffer<DataTuple>(SUB_QUERY_QUEUE_CAPACITY);
				c.getSubQuery().registerOutputQueue(down.getSubQuery().getId(), q);
				// register this queue as input of downstream 
				down.getSubQuery().registerInputQueue(c.getSubQuery().getId(), q);
			}
		}
		
		/*
		 *  Init map for subquery tasks
		 */
		for (ISubQueryConnectable c : this.subQueries)
			runningSubQueryTasks.put(c, new LinkedList<SubQueryTask>());
		
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<ISubQueryConnectable> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
}
