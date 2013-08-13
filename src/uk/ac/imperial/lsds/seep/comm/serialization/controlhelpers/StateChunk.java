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

	private int opId;
//	private int partitionNumber;
	private int sequenceNumber;
	private int totalChunks;
	private MemoryChunk mc;
//	private State state;
//	private ArrayList<Integer> partitioningRange;
	

	public StateChunk(){}
	
//	public StateChunk(int opId, int partitionNumber, int sequenceNumber, int totalChunks, State state, ArrayList<Integer> partitioningRange){
//		this.opId = opId;
//		this.partitionNumber = partitionNumber;
//		this.sequenceNumber = sequenceNumber;
//		this.totalChunks = totalChunks;
//		this.state = state;
//		this.partitioningRange = partitioningRange;
//	}
	
	public StateChunk(int opId, int seqNumber, int totalChunks, MemoryChunk mc){
		this.opId = opId;
		this.sequenceNumber = seqNumber;
		this.totalChunks = totalChunks;
		this.mc = mc;
	}
	
	public int getOpId() {
		return opId;
	}

	public void setOpId(int opId) {
		this.opId = opId;
	}
	
	public MemoryChunk getMemoryChunk(){
		return mc;
	}

//	public int getPartitionNumber() {
//		return partitionNumber;
//	}
//
//	public void setPartitionNumber(int partitionNumber) {
//		this.partitionNumber = partitionNumber;
//	}

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
	
//	public void setState(State state){
//		this.state = state;
//	}
//	
//	public State getState(){
//		return state;
//	}
//	
//	public ArrayList<Integer> getPartitioningRange() {
//		return partitioningRange;
//	}
//
//	public void setPartitioningRange(ArrayList<Integer> partitioningRange) {
//		this.partitioningRange = partitioningRange;
//	}
}
