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

import java.io.Serializable;

public abstract class RootTuple implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int tupleSchemaId;
	private long timestamp;
	private int id;
	
	public int getTupleSchemaId() {
		return tupleSchemaId;
	}
	public void setTupleSchemaId(int tupleSchemaId) {
		this.tupleSchemaId = tupleSchemaId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public RootTuple(){
		
	}
	
	public RootTuple(int tupleSchemaId, long timestamp, int id){
		this.tupleSchemaId = tupleSchemaId;
		this.timestamp = timestamp;
		this.id = id;
	}
	
}
