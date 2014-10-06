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

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutorService;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.MultiAPI;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

import uk.ac.imperial.lsds.seep.operator.compose.executor.CPUExecutorService;

public class MultiOperator implements StatelessOperator {
	
	/* Simple flag to switch to the GPU executor service. */
	private final boolean GPU = false;
	private GPUExecutionContext gpu = null;
	
	/* Print statistics */
	long target = 0L;
	long tuples = 0L;
	long start = 0L;
	long dt;
	double rate;
	long dtuples, previous_tuples, current_time;
	
	/* GPU context static configuration */
	int panes; // = 600;
 	// int max_keys = 200;
	// int panes_per_window = 300;
	int max_tuples_per_pane; // = 2000 * Integer.valueOf(GLOBALS.valueFor("L"));

	final private Logger LOG = LoggerFactory.getLogger(MultiOperator.class);
	
	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<ISubQueryConnectable> subQueries;
	private MultiAPI api;
	private boolean singleUpstreamBuffer;
	private Set<ISubQueryConnectable> mostUpstreamSubQueries;
	private Set<ISubQueryConnectable> mostDownstreamSubQueries;
	
	private Connectable parentConnectable; 
	
	private ExecutorService executorService;
	
	private SubQueryBufferWindowWrapper single_buffer;
	
	private MultiOperator(Set<ISubQueryConnectable> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
		for (ISubQueryConnectable c : this.subQueries)
			c.setParentMultiOperator(this);
	}
	
	public void setParentConnectable(Connectable parentConnectable) {
		this.parentConnectable = parentConnectable;
	}
	
	public GPUExecutionContext getGPUContext() { 
		return gpu; 
	}
	
	public boolean isGPUEnabled() { 
		return GPU; 
	}
	
	public long getTarget() { 
		return target; 
	}
	
	
	public void targetReached() {
	/*	
		dt = (System.currentTimeMillis() - start) / 1000.;
		rate =  tuples / dt;
		
		System.out.println(String.format("%10d tuples processed", tuples));
		System.out.println(String.format("%10.1f seconds", dt));
		System.out.println(String.format("%10.1f tuples/s", rate));
		
		if (GPU) 
			gpu.stats();
	*/
	}
	
	/**
	 * Note that pushing the data to the buffers of the most upstream 
	 * operators is not threadsafe. We assume a single thread to call 
	 * processData.
	 */
	public void processData(MultiOpTuple data, MultiAPI api) {
		/*
		 * Store the api so that it can be later used to forward tuples
		 */
		this.api = api;
		
		if (tuples == 0) {
			start = System.currentTimeMillis();
			previous_tuples = 0;
		}
		/*
		 * Try to push to all input buffers of the most upstream sub queries
		 */
		//for (ISubQueryConnectable q : this.mostUpstreamSubQueries) {
		//	for (SubQueryBufferWindowWrapper bw : q.getLocalUpstreamBuffers().values()) {
		//		if (!singleUpstreamBuffer)
		//			data = new MultiOpTuple(data);
				
				// bw.addToBuffer(data);
		//		tuples++;
				// System.out.println(tuples);
		//	}
		//}
		
		//SubQueryBufferWindowWrapper bw = 
		//	this.mostUpstreamSubQueries.iterator().next().getLocalUpstreamBuffers().values().iterator().next();
		
		single_buffer.addToBuffer(data);
		
		tuples ++;
		
		if (tuples % 60000000 == 0) {
			current_time = System.currentTimeMillis();
			dt = current_time - start;
			dtuples = tuples - previous_tuples;
			rate = (double) dtuples / ((double) dt / 1000.);
			System.out.println(
			String.format("[DBG] [MultiOperator] %13d tuples | %10d msec |%15.2f tuples/sec", tuples, dt, rate)
			);
			previous_tuples = tuples;
			start = current_time;
		}
	}

	
	@Override
	public void processData(DataTuple data, API api) {
		/*
		 * Deactivated for local testing
		 */
//		processData(MultiOpTuple.newInstance(data), api);
	}
	
	@Override
	public void processData(List<DataTuple> dataList, API localApi) {
		/*
		 * Deactivated for local testing
		 */
//		for (DataTuple tuple : dataList)
//			this.processData(tuple, localApi);
	}

	@Override
	public void setUp() {
		
		/*
		 * Create the thread pool 
		 */
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		//TODO: think about tuning this selection
		int numberOfCoresToUse = Math.max(numberOfCores, subQueries.size());
		numberOfCoresToUse = Integer.valueOf(GLOBALS.valueFor("numcores"));
		System.out.println(numberOfCoresToUse + " available processors");
		
		if (GPU) {
			System.out.println("Launching GPU executor service...");
			// gpu = new GPUExecutionContext(panes, max_keys, panes_per_window, max_tuples_per_pane);
			panes = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));
			max_tuples_per_pane = 1024;
			System.out.println(String.format("%d panes %d tuples/pane", panes, max_tuples_per_pane));
			gpu = new GPUExecutionContext(panes, max_tuples_per_pane);
		 	this.executorService = new GPUExecutorService(1000);
		} else {
			/* */
			// this.executorService = new CPUExecutorService(3, 10000);
			this.executorService = Executors.newFixedThreadPool(numberOfCoresToUse);
			// this.executorService = new ThreadPoolExecutor(numberOfCoresToUse, numberOfCoresToUse, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10000, true));
			// this.executorService = Executors.newFixedThreadPool(1);
		}

		target = (long)Math.floor((2999d - 300d) / Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"))) + 1; 
//		target = 1;
		
		/*
		 * Identify most upstream and most downstream local operators
		 * and create input/output buffers for them
		 */
		this.mostUpstreamSubQueries = new HashSet<>();
		this.mostDownstreamSubQueries = new HashSet<>();
		for (ISubQueryConnectable connectable : subQueries){
			connectable.setUp();
			if (connectable.isMostLocalUpstream()) {
				this.mostUpstreamSubQueries.add(connectable);

				for (Integer streamID : connectable.getWindowDefinitions().keySet()) 
					connectable.registerLocalUpstreamBuffer(new SubQueryBufferWindowWrapper(connectable, streamID), streamID);
			}
			if (connectable.isMostLocalDownstream()) {
				this.mostDownstreamSubQueries.add(connectable);
				connectable.addResultForwarder(new SubQueryTaskResultAPIForwarder(this));
			}
		}
		
		this.singleUpstreamBuffer = false;
		if (this.mostUpstreamSubQueries.size() == 1) {
			System.out.println("_______1 subquery upstream");
			this.singleUpstreamBuffer = (this.mostUpstreamSubQueries.iterator().next().getLocalUpstreamBuffers().keySet().size() == 1);
		}
		
		single_buffer = this.mostUpstreamSubQueries.iterator().next().getLocalUpstreamBuffers().values().iterator().next();
	}

	public MultiAPI getAPI() {
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
