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
package uk.ac.imperial.lsds.seep.operator.compose2;

import java.util.ArrayList;
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
import uk.ac.imperial.lsds.seep.operator.WindowOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose2.Window;

public class MultiOperator implements OperatorCode {

	private static final long serialVersionUID = 1L;

	private final int id;
	
	private API distributedAPI;

	private API localAPI;

	private ExecutorService executorService;
	
	private List<WindowOperatorCode> operators;
	
	private MultiOpInputList inputList;

	private MultiOpOutputList outputList;

	private MultiOperator(List<WindowOperatorCode> operators, int multiOpId){
		this.id = multiOpId;
		this.operators = operators;
		this.inputList = new MultiOpInputList();
		this.outputList = new MultiOpOutputList();
	}
	
	public void setDistributedApi(API distributedAPI) {
		this.distributedAPI = distributedAPI;
	}
	
	@Override
	public void processData(DataTuple data, API api) {
		this.distributedAPI = api;
		this.inputList.add(data);
	}
	
	@Override
	public void processData(List<DataTuple> dataList, API api) {
		for (DataTuple tuple : dataList)
			this.processData(tuple, api);
	}

	@Override
	public void setUp() {
		
		/*
		 * Create the thread pool 
		 */
		//TODO: think about this selection
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		this.executorService = Executors.newFixedThreadPool(numberOfCores);

	}
	
	private void submitTasks() {
		
		/*
		 * 1) when to trigger this method?
		 */
		
		/*
		 * 2) determine how to create the tasks
		 */
		
		Window window = new RowBasedWindow(inputList, outputList, 0, 100, 10, 5);
		
		MicroOperatorTask task = new MicroOperatorTask(operators, window);
		
		this.executorService.submit(task, new ArrayList<DataTuple>());
		
		
		
	}
	
}
