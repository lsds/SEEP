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
package uk.ac.imperial.lsds.seep.operator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class DistributedApi implements API, CommunicationPrimitives, Serializable{

	private static final long serialVersionUID = 1L;
	private static DistributedApi instance = new DistributedApi();
	private Operator op;
	
	@Override
	public void setCallbackObject(Callback c) {
		this.op = (Operator)c;
	}
	
	public static DistributedApi getInstance(){
		return instance;
	}

	private DistributedApi(){}
	
	// Communication primitives
	
	@Override
	public synchronized void send(DataTuple dt){
		op.send(dt);
	}
	
	@Override
	public synchronized void send_toIndex(DataTuple dt, int idx){
		op.send_toIndex(dt, idx);
	}
	
	@Override
	public synchronized void send_splitKey(DataTuple dt, int key){
		op.send_splitKey(dt, key);
	}
	
	@Override
	public synchronized void send_toStreamId(DataTuple dt, int streamId){
		op.send_toStreamId(dt, streamId);
	}
	
	@Override
	public synchronized void send_toStreamId_splitKey(DataTuple dt, int streamId, int key){
		op.send_toStreamId_splitKey(dt, streamId, key);
	}
	
	@Override
	public synchronized void send_toStreamId_toAll(DataTuple dt, int streamId){
		op.send_toStreamId_toAll(dt, streamId);
	}
	
	@Override
	public void send_all(DataTuple dt){
		op.send_all(dt);
	}
	
	// Other
	
	public int getOperatorId(){
		return op.getOperatorId();
	}
	
	public Map<String, Integer> getDataMapper(){
		Map<String, Integer> mapper = new HashMap<String, Integer>();
		for(int i = 0; i<op.getOpContext().getDeclaredWorkingAttributes().size(); i++){
			mapper.put(op.getOpContext().getDeclaredWorkingAttributes().get(i), i);
		}
		return mapper;
	}
	
	// System configuration
	
	public void disableCheckpointing(){
		op.disableCheckpointing();
	}
	
	public void disableMultiCoreSupport(){
		op.disableMultiCoreSupport();
	}
}
