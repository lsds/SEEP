/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.MetricsReader;

public class InputQueue implements DataStructureI{

	private BlockingQueue<DataTuple> inputQueue;
	
	public InputQueue(){
		inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
	}
	
	public InputQueue(int size){
		inputQueue = new ArrayBlockingQueue<DataTuple>(size);
	}
	
	public synchronized void push(DataTuple data){
		try {
			inputQueue.put(data);
			MetricsReader.eventsInputQueue.inc();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean pushOrShed(DataTuple data){
		boolean inserted = inputQueue.offer(data);
		if (inserted) MetricsReader.eventsInputQueue.inc();
		return inserted;
	}
	
	public DataTuple pull(){
		try {
			MetricsReader.eventsInputQueue.dec();
			return inputQueue.take();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void clean(){
		try {
			MetricsReader.eventsInputQueue.dec();
			inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("INPUT QUEUE SIZE BEFORE CLEANING: "+MetricsReader.eventsInputQueue.getCount());
		System.out.println("BEFORE- REAL SIZE OF INPUT QUEUE: "+inputQueue.size());
		//MetricsReader.eventsInputQueue.clear();
		MetricsReader.reset(MetricsReader.eventsInputQueue);
		inputQueue.clear();
		System.out.println("AFTER- REAL SIZE OF INPUT QUEUE: "+inputQueue.size());
		System.out.println("INPUT QUEUE SIZE AFTER CLEANING: "+MetricsReader.eventsInputQueue.getCount());
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}
}
