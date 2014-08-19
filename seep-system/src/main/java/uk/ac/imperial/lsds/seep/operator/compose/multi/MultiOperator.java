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

public class MultiOperator implements StatelessOperator {
	
	/* Simple flag to switch to the GPU executor service. */
	private final boolean GPU = false;
	private GPUExecutionContext gpu = null;
	
	/* Print statistics */
	long target = 0L;
	long tuples = 0L;
	long start = 0L;
	double dt, rate;
	
	/* GPU context static configuration */
	int panes = 600;
	int max_keys = 200;
	int panes_per_window = 300;
	int max_tuples_per_pane = 2000;

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
	
	private MultiOperator(Set<ISubQueryConnectable> subQueries, int multiOpId){
		this.id = multiOpId;
		this.subQueries = subQueries;
		for (ISubQueryConnectable c : this.subQueries)
			c.setParentMultiOperator(this);
	}
	
	public void setParentConnectable(Connectable parentConnectable) {
		this.parentConnectable = parentConnectable;
	}
	
	public GPUExecutionContext getGPUContext() { return gpu; }
	public boolean isGPUEnabled() { return GPU; }
	public long getTarget() { return target; }
	public void targetReached() {
		dt = (double) (System.currentTimeMillis() - start) / 1000.;
		/* Stats */
		rate =  (double) (tuples) / dt;
		System.out.println(String.format("%10d tuples processed", tuples));
		System.out.println(String.format("%10.1f seconds", (double) dt));
		System.out.println(String.format("%10.1f tuples/s", rate));
		if (GPU) gpu.stats();
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
		
		if (tuples == 0)
			start = System.currentTimeMillis();
		/*
		 * Try to push to all input buffers of the most upstream sub queries
		 */
		for (ISubQueryConnectable q : this.mostUpstreamSubQueries) {
			for (SubQueryBufferWrapper bw : q.getLocalUpstreamBuffers().values()) {
				// Make sure to copy if there is more than one most upstream buffer
				if (!singleUpstreamBuffer)
					data = MultiOpTuple.newInstance(data);
				// This code is accessed by a single thread only
				while (!bw.add(data)) {
					try {
						synchronized (bw.getBuffer().getExternalLock()) {
							bw.getBuffer().getExternalLock().wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				tuples ++;
			}
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
		System.out.println(numberOfCoresToUse + " available processors");
		if (GPU) {
			gpu = new GPUExecutionContext(panes, max_keys, panes_per_window, max_tuples_per_pane);
		 	this.executorService = new GPUExecutorService(1000);
		} else {
			this.executorService = Executors.newFixedThreadPool(numberOfCoresToUse);
			// this.executorService = Executors.newFixedThreadPool(1);
		}

		target = (long)Math.floor(10499d / Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"))); 
//		target = 1;
		// start = System.currentTimeMillis();
		
		/*
		 * Identify most upstream and most downstream local operators
		 * and create input/output buffers for them
		 */
		this.mostUpstreamSubQueries = new HashSet<>();
		this.mostDownstreamSubQueries = new HashSet<>();
//		for (ISubQueryConnectable connectable : subQueries){
//			if (connectable.isMostLocalUpstream()) {
//				this.mostUpstreamSubQueries.add(connectable);
//				SubQueryBuffer b = new SubQueryBuffer();
//				for (Integer streamID : connectable.getSubQuery().getWindowDefinitions().keySet())
//					connectable.registerLocalUpstreamBuffer(b, streamID);
//			}
//			if (connectable.isMostLocalDownstream()) {
//				this.mostDownstreamSubQueries.add(connectable);
//				
//				SubQueryBuffer b = new SubQueryBuffer();
//				// deactivate for local testing
////				for (Integer streamID : parentConnectable.getOpContext().routeInfo.keySet())
////					connectable.registerLocalDownstreamBuffer(b, streamID);
//				connectable.registerLocalDownstreamBuffer(b, 0);
//			}
//		}
//		
//		this.singleUpstreamBuffer = (this.mostUpstreamSubQueries.size() == 1);
//		
//		/*
//		 * Start handlers for sub queries
//		 */
//		for (ISubQueryConnectable c : this.subQueries) {
//			/* 
//			 * Select appropriate forwarding mechanism:
//			 *  - default is writing to downstream sub query buffer 
//			 *  - if subquery is most downstream, forwarding to distributed nodes via API is enabled
//			 */
//			ISubQueryTaskResultForwarder resultForwarder = 
//				(c.isMostLocalDownstream())? 
//					new SubQueryTaskResultAPIForwarder(c)
//					: new SubQueryTaskResultBufferForwarder(c);
//			
//			//TODO: select task creation scheme
//			SubQueryHandler r = new SubQueryHandler(c, new WindowBatchTaskCreationScheme(c), resultForwarder);
//			(new Thread(r)).start();
//		}
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
