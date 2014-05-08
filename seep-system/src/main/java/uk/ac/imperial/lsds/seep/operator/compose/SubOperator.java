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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.CommunicationPrimitives;
import uk.ac.imperial.lsds.seep.operator.LocalApi;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class SubOperator implements SubOperatorAPI, CommunicationPrimitives, Callback{

	private static final long serialVersionUID = 1L;
	
	private OperatorCode code;
	private LocalApi api;
	
	private MultiOperator multiOp;
	
	private boolean mostDownstream;
	private boolean mostUpstream;
	private Map<Integer, SubOperator> localDownstream;
	private Map<Integer, SubOperator> localUpstream;
	
	public static SubOperator getSubOperator(OperatorCode code){
		return new SubOperator(code);
	}
	
	private SubOperator(OperatorCode code){
		this.code = code;
		this.mostDownstream = true;
		this.mostUpstream = true;
		this.localDownstream = new HashMap<Integer, SubOperator>();
		this.localUpstream = new HashMap<Integer, SubOperator>();
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
				target.processData(dt, api);
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
			target.processData(dt, api);
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
    public void send_to_OpId(DataTuple dt, int opId) {
        
    }

    @Override
    public void send_to_OpIds(DataTuple[] dt, int[] opId) {
        
    }

    @Override
    public void send_toIndices(DataTuple[] dts, int[] indices) {
        
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

	public void processData(DataTuple data, API localApi) {
		// We set our own reference as callback and then call processData
		this.api = (LocalApi) localApi;
		localApi.setCallbackObject(this);
		code.processData(data, localApi);
	}

	public void processData(List<DataTuple> dataList, API localApi) {
		localApi.setCallbackObject(this);
		this.api = (LocalApi) localApi;
		code.processData(dataList, localApi);
	}

	@Override
	public void setUp() {
		code.setUp();
	}

	public void setMultiOperator(MultiOperator multiOperator) {
		this.multiOp = multiOp;
	}
}
