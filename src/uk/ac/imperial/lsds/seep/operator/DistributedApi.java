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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class DistributedApi implements API, CommunicationPrimitives, Serializable{

	private static final long serialVersionUID = 1L;
	private Operator op;
	private final Logger LOG = LoggerFactory.getLogger(DistributedApi.class);

	
	@Override
	public void setCallbackObject(Callback c) {
		this.op = (Operator)c;
	}
	
	public DistributedApi(){}
	
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
        
        @Override
        public synchronized void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId){
                op.send_toStreamId_toAll_threadPool(dt, streamId);
        }
        
        @Override
        public synchronized void send_all_threadPool(DataTuple dt){
                op.send_all_threadPool(dt);
        }
        
        @Override
	public synchronized void send_to_OpId(DataTuple dt, int opId){
                int opIndex = op.getOpContext().findOpIndexFromDownstream(opId);
                this.send_toIndex(dt, opIndex);
        }
        
        @Override
        public synchronized void send_to_OpIds(DataTuple[] dt, int[] opId){
            int[] indices = new int[opId.length];
            int opIndex;
            for(int i = 0 ; i < opId.length ; i++){
                indices[i] = op.getOpContext().findOpIndexFromDownstream(opId[i]);
            }
            this.send_toIndices(dt, indices);
        }
        
        @Override
        public synchronized void send_toIndices(DataTuple[] dts, int[] indices){
            op.send_toIndices(dts, indices);
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
