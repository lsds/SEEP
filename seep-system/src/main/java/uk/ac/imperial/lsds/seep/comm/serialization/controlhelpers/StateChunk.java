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

import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;

public class StateChunk {

	private int ownerOpId;
	private int keeperOpId;
	private int sequenceNumber;
	private int totalChunks;
	private MemoryChunk mc;
	private int splittingKey;

	public StateChunk(){}
	
	public StateChunk(int opId, int keeperOpId, int seqNumber, int totalChunks, MemoryChunk mc, int splittingKey){
		this.ownerOpId = opId;
		this.keeperOpId = keeperOpId;
		this.sequenceNumber = seqNumber;
		this.totalChunks = totalChunks;
		this.mc = mc;
		this.splittingKey = splittingKey;
	}
	
	public int getOwnerOpId() {
		return ownerOpId;
	}

	public void setOwnerOpId(int opId) {
		this.ownerOpId = opId;
	}
	
	public int getKeeperOpId(){
		return keeperOpId;
	}
	
	public void setKeeperOpId(int keeperOpId){
		this.keeperOpId = keeperOpId;
	}
	
	public MemoryChunk getMemoryChunk(){
		return mc;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public int getTotalChunks(){
		return totalChunks;
	}
	
	public void setTotalChunks(int totalChunks){
		this.totalChunks = totalChunks;
	}

	public void setSplittingKey(int key){
		splittingKey = key;
	}
	
	public int getSplittingKey() {
		return splittingKey;
	}
}
