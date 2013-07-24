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
package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

public class RawData {

	private int opId;
	private long ts;
	private byte[] data;
	
	public RawData(){}
	
	public RawData(int nodeId, byte[] data){
		this.opId = nodeId;
		this.data = data;
	}
	
	public int getOpId() {
		return opId;
	}
	
	public void setOpId(int nodeId) {
		this.opId = nodeId;
	}
	
	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
}