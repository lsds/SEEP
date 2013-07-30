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
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.QuerySpecificationI.InputDataIngestionMode;

public class DataStructureAdapter {
	
	private Map<Integer, DataStructureI> dsoMap = new HashMap<Integer, DataStructureI>();
	private DataStructureI uniqueDso = null;
	
	public DataStructureAdapter(){
		
	}
	
	public DataStructureI getUniqueDso(){
		return uniqueDso;
	}
	
	public DataStructureI getDataStructureIForOp(int opId){
		return dsoMap.get(opId);
	}
	
	public int getNumberOfModes(){
		return dsoMap.size();
	}
	
	public Map<Integer, DataStructureI> getInputDataIngestionModeMap(){
		return dsoMap;
	}
	
	public void setDSOForOp(int opId, DataStructureI dso){
		dsoMap.put(opId, dso);
	}
	
//	public void push(DataTuple dt){
//		uniqueDso.push(dt);
//	}
//	
//	public DataTuple pull(){
//		return uniqueDso.pull();
//	}
//	
//	public ArrayList<DataTuple> pullBarrier(){
//		return uniqueDso.pull_from_barrier();
//	}
	
	public void setUp(Map<Integer, InputDataIngestionMode> iimMap, int numUpstreams){
		// Differentiate between cases with only one inputdatamode and more than one (for performance reasons)
		if(iimMap.size() > 1){
			// For processing one event per iteration, the queue is the best abstraction
			for(Entry<Integer, InputDataIngestionMode> entry : iimMap.entrySet()){
				if(entry.getValue().equals(Operator.InputDataIngestionMode.ONE_AT_A_TIME)){
					InputQueue iq = new InputQueue();
					dsoMap.put(entry.getKey(), iq);
				}
				else if(entry.getValue().equals(Operator.InputDataIngestionMode.UPSTREAM_SYNC_BARRIER)){
					Barrier b = new Barrier(numUpstreams);
					dsoMap.put(entry.getKey(), b);
				}
			}
		}
		else if(iimMap.size() == 1){
			for(Entry<Integer, InputDataIngestionMode> entry : iimMap.entrySet()){
				if(entry.getValue().equals(Operator.InputDataIngestionMode.ONE_AT_A_TIME)){
					InputQueue iq = new InputQueue();
					uniqueDso = iq;
				}
				else if(entry.getValue().equals(Operator.InputDataIngestionMode.UPSTREAM_SYNC_BARRIER)){
					Barrier b = new Barrier(numUpstreams);
					uniqueDso= b;
				}
			}
		}
	}
	
	/** SPECIFIC METHODS **/
	
	/// \fixme{REQUIRES opID to understand how to update the barrier.}
	/// \fixme{In general it is necessary to revisit these methods when downstream and upstreams are dynamically added}
	@Deprecated
	public void reconfigureNumUpstream(int upstreamSize){
		// Number of upstream has changed, this affects the barrier
		System.out.println("NEW UPSTREAM SIZE: "+upstreamSize);
//		if(dso instanceof Barrier){
//			System.out.println("Calling to reconfigure barrier");
//			((Barrier)dso).reconfigureBarrier(upstreamSize);
//		}
	}
}
