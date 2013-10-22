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
package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;

public class BatchDataTuple {
	
	private ArrayList<DataTuple> batch = new ArrayList<DataTuple>();
	private int batchSize = 0;
	
	public void addTuple(DataTuple data){
		batch.add(data);
		batchSize++;
	}
	
	public void clear(){
		batch = new ArrayList<DataTuple>();
		batchSize = 0;
	}
	
	public BatchDataTuple getBatch(){
		return this;
	}
	
	public int getBatchSize(){
		return batchSize;
	}
	
	public DataTuple getTuple(int index){
		return batch.get(index);
	}
	
	public ArrayList<DataTuple> getTuples(){
		return batch;
	}
	
	public BatchDataTuple(){
		
	}
}
