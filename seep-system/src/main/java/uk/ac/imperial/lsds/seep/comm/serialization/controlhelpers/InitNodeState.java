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

public class InitNodeState {

	private int opId;
	private InitOperatorState initOperatorState;
	//
	private int senderOperatorId;
	
	public int getSenderOperatorId() {
		return senderOperatorId;
	}
	public void setSenderOperatorId(int senderOperatorId) {
		this.senderOperatorId = senderOperatorId;
	}
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public InitOperatorState getInitOperatorState() {
		return initOperatorState;
	}
	public void setInitOperatorState(InitOperatorState initOperatorState) {
		this.initOperatorState = initOperatorState;
	}
	
	public InitNodeState(){	}
	
	public InitNodeState(int senderOperatorId, int nodeId, InitOperatorState initOperatorState){
		this.senderOperatorId = senderOperatorId;
		this.opId = nodeId;
		this.initOperatorState = initOperatorState;
	}
	
}
