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
package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutorService;

public class MultiOperator {
	
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
	
	private Set<SubQuery> subQueries;
	
	private IQueryBuffer mostUpstreamBuffer;

	private ExecutorService executorService;
	
	public MultiOperator(Set<SubQuery> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
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
	
	public void processData (byte[] values) {
		if (tuples == 0) {
			start = System.currentTimeMillis();
			previous_tuples = 0;
		}
		
		this.mostUpstreamBuffer.put(values);
		
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
		
		for (SubQuery sb : this.subQueries)
			if (sb.isMostUpstream())
				this.mostUpstreamBuffer = sb.getInputBuffer();
	}
	
	public int getMultiOpId(){
		return id;
	}
	
	public ExecutorService getExecutorService() {
		return this.executorService;
	}
	
}
