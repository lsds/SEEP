/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Added calls to notify arrival and departure of tuples
 *     to DefaultMetricsReader.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.DefaultMetricsNotifier.notifyThat;

public class InputQueue implements DataStructureI{

	private BlockingQueue<DataTuple> inputQueue;
	
	public InputQueue(){
		inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
		//inputQueue = new LinkedBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
		//Unbounded
		//inputQueue = new LinkedBlockingQueue<DataTuple>();
	}
	
	public InputQueue(int size){
		inputQueue = new ArrayBlockingQueue<DataTuple>(size);
	}
	
	public synchronized void push(DataTuple data){
		try {
			inputQueue.put(data);
            
            // Seep monitoring
            notifyThat(0).inputQueuePut();
            
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean pushOrShed(DataTuple data){
		boolean inserted = inputQueue.offer(data);
		if (inserted) {
            // Seep monitoring
            notifyThat(0).inputQueuePut();
        }
        
		return inserted;
	}
	
	public DataTuple[] pullMiniBatch(){
		int miniBatchSize = 10;
		DataTuple[] batch = new DataTuple[miniBatchSize];

        // Seep monitoring: notify reset of input queue
        notifyThat(0).inputQueueTake();
            
        for(int i = 0; i<miniBatchSize; i++){
			DataTuple dt = inputQueue.poll();
			if(dt != null)
				batch[i] = dt;
			else
				break;
		}
		return batch;
	}
	
	public DataTuple pull(){
		try {
            // Seep monitoring
            notifyThat(0).inputQueueTake();
            
			return inputQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void clean(){
		try {
            // Seep monitoring
            notifyThat(1).inputQueueTake();
        
            inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BEFORE- REAL SIZE OF INPUT QUEUE: " + inputQueue.size());
		
        // Seep monitoring: notify reset of input queue
        notifyThat(0).inputQueueReset();
        
		inputQueue.clear();
		System.out.println("AFTER- REAL SIZE OF INPUT QUEUE: " + inputQueue.size());
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<FailureCtrl> purge(FailureCtrl nodeFctrl) {
		throw new RuntimeException("TODO");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}
	
	
}
