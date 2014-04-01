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

public class InvalidateState {

	private int operatorId;

	public InvalidateState(){
	}
	
	public InvalidateState(int nodeId){
		this.operatorId = nodeId;
	}
	
	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int nodeId) {
		this.operatorId = nodeId;
	}
}
