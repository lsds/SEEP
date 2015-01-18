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

public class CloseSignal {

	private int opId;
	private int totalNumberOfChunks;

	public CloseSignal(){}
	
	public CloseSignal(int opId, int totalNumberOfChunks){
		this.opId = opId;
		this.totalNumberOfChunks = totalNumberOfChunks;
	}
	
	public int getOpId() {
		return opId;
	}
	
	public void setOpId(int opId) {
		this.opId = opId;
	}
	
	public int getTotalNumberOfChunks() {
		return totalNumberOfChunks;
	}

	public void setTotalNumberOfChunks(int totalNumberOfChunks) {
		this.totalNumberOfChunks = totalNumberOfChunks;
	}
	
}
