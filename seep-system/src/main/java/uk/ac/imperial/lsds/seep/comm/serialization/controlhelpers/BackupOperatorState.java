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
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;
import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.buffer.OutputBuffer;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class BackupOperatorState implements Serializable{

	private static final long serialVersionUID = 1L;

	private int opId;
	
	private StateWrapper state;
	private ArrayList<OutputBuffer> outputBuffers = null;
	private String stateClass;
	
	public BackupOperatorState(){}
	
	public void setStateClass(String stateClass){
		this.stateClass = stateClass;
	}
	
	public String getStateClass(){
		return stateClass;
	}
	
	public int getOpId() {
		return opId;
	}
	
	public void setOpId(int opId) {
		this.opId = opId;
	}
	
	public StateWrapper getState() {
		return state;
	}
	
	public void setState(StateWrapper state) {
		this.state = state;
	}
	
	public ArrayList<OutputBuffer> getOutputBuffers(){
		return outputBuffers;
	}
	
	public void setOutputBuffers(ArrayList<OutputBuffer> outputBuffers){
		this.outputBuffers = outputBuffers;
	}
	
}
