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

import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class InitOperatorState {

	private int opId;
	private StateWrapper state;
	
	public InitOperatorState(){
		
	}
	
	public InitOperatorState(int opId, StateWrapper state){
		this.opId = opId;
		this.state = state;
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
}
