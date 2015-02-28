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
import java.util.List;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.CommunicationPrimitives;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class MultiOperator implements OperatorCode, ComposedOperator, CommunicationPrimitives{

	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<SubOperator> subOperators;
	private SubOperator mostUpstream;
	
	private MultiOperator(Set<SubOperator> subOperators, int multiOpId){
		this.id = multiOpId;
		if(checkConstraints(subOperators)){
			this.subOperators = subOperators;
		}
		else{
			//TODO throw error
		}
	}
	
	private boolean checkConstraints(Set<SubOperator> subOperators){
		// TODO:
		// - constains:
		// - 1 single upstream and 1 single downstream
		// - ...
		return true;
	}
	
	/** Implementation of OperatorCode interface **/
	
	@Override
	public void processData(DataTuple data) {
		// just call the first op to start processing data
		mostUpstream.processData(data);
	}

	@Override
	public void processData(List<DataTuple> dataList) {
		mostUpstream.processData(dataList);
	}

	@Override
	public void setUp() {
		for(SubOperator so : subOperators){
			so.setMultiOperator(this);
			so.setUp();
		}
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<SubOperator> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
	@Override
	public int getNumberOfSubOperators() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isComposedOperatorStateful() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void send(DataTuple dt) {
		api.send(dt);
	}
	
	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		api.send_toStreamId(dt, streamId);
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
    public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
        
    }

    @Override
    public void send_all_threadPool(DataTuple dt) {
        
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
	
	@Override
	public void ack(DataTuple dt) {
		// TODO Auto-generated method stub
		
	}
	
	
}
