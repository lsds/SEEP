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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class BufferedBarrier implements DataStructureI{

	private List<ArrayBlockingQueue<DataTuple>> buffers = new ArrayList<ArrayBlockingQueue<DataTuple>>();
	private Map<Long, Integer> thread_mapper = new HashMap<Long, Integer>();
	
	@Override
	public DataTuple pull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		ArrayList<DataTuple> toReturn = new ArrayList<DataTuple>();
		for(ArrayBlockingQueue<DataTuple> buffer : buffers){
			try {
				toReturn.add(buffer.take());
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return toReturn;
	} 

	@Override
	public void push(DataTuple dt) {
		long threadId = Thread.currentThread().getId();
		int idx = -1;
		// If already exists
		if(thread_mapper.containsKey(threadId)){
			idx = thread_mapper.get(threadId);
		}
		// Otherwise we register the thread
		else{
			idx = register();
		}
		try {
			buffers.get(idx).put(dt);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int register(){
		long id = Thread.currentThread().getId();
		ArrayBlockingQueue<DataTuple> buffer = new ArrayBlockingQueue<DataTuple>(1000);
		buffers.add(buffer);
		int idx = buffers.size()-1;
		thread_mapper.put(id, idx);
		return idx;
	}

}
