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

public class ReplayStateInfo {

	private int oldOpId;
	private int newOpId;
	private boolean streamToSingleNode;
	
	public ReplayStateInfo(){
		
	}
	
	public ReplayStateInfo(int oldOpId, int newOpId, boolean singleNode){
		// Old an new opIds for cases when an operator scales out
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
		// Streaming to a single node means that the node failed or the state is being reset for other unknown reason
		this.streamToSingleNode = singleNode;
	}
	
	public int getOldOpId() {
		return oldOpId;
	}

	public void setOldOpId(int oldOpId) {
		this.oldOpId = oldOpId;
	}

	public int getNewOpId() {
		return newOpId;
	}

	public void setNewOpId(int newOpId) {
		this.newOpId = newOpId;
	}

	public boolean isStreamToSingleNode() {
		return streamToSingleNode;
	}

	public void setSingleNode(boolean singleNode) {
		this.streamToSingleNode = singleNode;
	}
}
