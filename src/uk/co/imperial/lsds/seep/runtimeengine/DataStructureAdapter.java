/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.co.imperial.lsds.seep.operator.Operator;

public class DataStructureAdapter {

	private DataStructureI dso;
	
	public DataStructureI getDSO(){
		return dso;
	}
	
	public void setDSO(DataStructureI dso){
		this.dso = dso;
	}
	
	public DataStructureAdapter(){
		
	}
	
	public void push(DataTuple dt){
		dso.push(dt);
	}
	
	public DataTuple pull(){
		return dso.pull();
	}
	
	public ArrayList<DataTuple> pullBarrier(){
		return dso.pull_from_barrier();
	}
	
	public void setUp(Operator.DataAbstractionMode dam, int numUpstreams){
		// For processing one event per iteration, the queue is the best abstraction
		if(dam.equals(Operator.DataAbstractionMode.ONE_AT_A_TIME)){
			InputQueue iq = new InputQueue();
			dso = iq;
		}
		else if(dam.equals(Operator.DataAbstractionMode.UPSTREAM_SYNC_BARRIER)){
			Barrier b = new Barrier(numUpstreams);
			dso = b;
		}
	}
	
	/** SPECIFIC METHODS **/
	
	public void reconfigureNumUpstream(int upstreamSize){
		// Number of upstream has changed, this affects the barrier
		System.out.println("NEW UPSTREAM SIZE: "+upstreamSize);
		if(dso instanceof Barrier){
			System.out.println("Calling to reconfigure barrier");
			((Barrier)dso).reconfigureBarrier(upstreamSize);
		}
	}
}
