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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.QuerySpecificationI.InputDataIngestionMode;

public class DataStructureAdapter {
	
	private Map<Integer, DataStructureI> dsoMap = new HashMap<Integer, DataStructureI>();
	
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
	
	public DataStructureAdapter(){
		
	}
	
//	public void push(DataTuple dt){
//		dso.push(dt);
//	}
//	
//	public DataTuple pull(){
//		return dso.pull();
//	}
//	
//	public ArrayList<DataTuple> pullBarrier(){
//		return dso.pull_from_barrier();
//	}
	
	public void setUp(Map<Integer, InputDataIngestionMode> iimMap, int numUpstreams){
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
