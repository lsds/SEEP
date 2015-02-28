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
package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.CommunicationPrimitives;

public class SubOperator implements SubOperatorAPI, CommunicationPrimitives, Callback{

	private static final long serialVersionUID = 1L;
	
	private SubOperatorCode code;
	private MultiOperator multiOp;
	
	private boolean mostDownstream;
	private boolean mostUpstream;
	private Map<Integer, SubOperator> localDownstream;
	private Map<Integer, SubOperator> localUpstream;
	
	public static SubOperator getSubOperator(SubOperatorCode code){
		return new SubOperator(code);
	}
	
	private SubOperator(SubOperatorCode code){
		this.code = code;
		this.mostDownstream = true;
		this.mostUpstream = true;
		this.localDownstream = new HashMap<Integer, SubOperator>();
		this.localUpstream = new HashMap<Integer, SubOperator>();
		//code.api.setCallbackObject(this);
	}
	
	private void addLocalDownstream(int localStreamId, SubOperator so){
		this.mostDownstream = false;
		if(!localDownstream.containsKey(localStreamId)){
			localDownstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	private void addLocalUpstream(int localStreamId, SubOperator so){
		this.mostUpstream = false;
		if(!localUpstream.containsKey(localStreamId)){
			localUpstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	/** Implementation of CommunicationPrimitives **/
	
	@Override
	public void send(DataTuple dt) {
		if(localDownstream.size() == 1){
			SubOperator target = localDownstream.entrySet().iterator().next().getValue();
			if(!target.isMostLocalDownstream()){
				target.processData(dt);
			}
			else{
				multiOp.send(dt);
			}
		}
		else{
			//Throw error, as downstream cannot be scaled out inside the node, (or can they?) there should be only one entry 
		}
	}
	
	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		SubOperator target = localDownstream.get(streamId);
		if(!target.isMostLocalDownstream()){
			target.processData(dt);
		}
		else{
			multiOp.send_toStreamId(dt, streamId);
		}
	}

	@Override
	public void send_all(DataTuple dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_splitKey(DataTuple dt, int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toIndex(DataTuple dt, int idx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_toStreamId_toAll(DataTuple dt, int streamId) {
		// TODO Auto-generated method stub
		
	}
        
        @Override
        public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId){
                
        }
        
        @Override
        public void send_all_threadPool(DataTuple dt){
                
        }
        
    	@Override
    	public void ack(DataTuple dt) {
    		// TODO Auto-generated method stub
    		
    	}
	
	/** Implementation of SubOperatorAPI **/
	
	@Override
	public void connectSubOperatorTo(int localStreamId, SubOperator so){
		this.addLocalDownstream(localStreamId, so);
		so.addLocalUpstream(localStreamId, this);
	}

	@Override
	public boolean isMostLocalDownstream() {
		return mostDownstream;
	}

	@Override
	public boolean isMostLocalUpstream() {
		return mostUpstream;
	}

	@Override
	public void processData(DataTuple data) {
		code.processData(data);
	}

	@Override
	public void processData(List<DataTuple> dataList) {
		code.processData(dataList);
	}

	@Override
	public void setUp() {
		code.setUp();
	}

	public void setMultiOperator(MultiOperator multiOperator) {
		this.multiOp = multiOp;
	}

    @Override
    public void send_to_OpId(DataTuple dt, int opId) {
        
    }

    @Override
    public void send_to_OpIds(DataTuple[] dt, int[] opId) {
        
    }

    @Override
    public void send_toIndices(DataTuple[] dts, int[] indices) {
        
    }

	@Override
	public void send_lowestCost(DataTuple dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_highestWeight(DataTuple dt) {
		// TODO Auto-generated method stub
		
	}
}
