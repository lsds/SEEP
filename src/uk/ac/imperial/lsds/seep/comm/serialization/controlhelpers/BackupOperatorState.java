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

import uk.ac.imperial.lsds.seep.operator.State;

public class BackupOperatorState implements Serializable{

	private static final long serialVersionUID = 1L;

	private int opId;
	
	private State state;
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
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
}
