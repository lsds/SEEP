package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.operator.State;

public class StateChunk {

	private int opId;
	private int partitionNumber;
	private int sequenceNumber;
	private int totalChunks;
	private State state;
	private ArrayList<Integer> partitioningRange;
	

	public StateChunk(){}
	
	public StateChunk(int opId, int partitionNumber, int sequenceNumber, int totalChunks, State state, ArrayList<Integer> partitioningRange){
		this.opId = opId;
		this.partitionNumber = partitionNumber;
		this.sequenceNumber = sequenceNumber;
		this.totalChunks = totalChunks;
		this.state = state;
		this.partitioningRange = partitioningRange;
	}
	
	public int getOpId() {
		return opId;
	}

	public void setOpId(int opId) {
		this.opId = opId;
	}

	public int getPartitionNumber() {
		return partitionNumber;
	}

	public void setPartitionNumber(int partitionNumber) {
		this.partitionNumber = partitionNumber;
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
	
	public void setState(State state){
		this.state = state;
	}
	
	public State getState(){
		return state;
	}
	
	public ArrayList<Integer> getPartitioningRange() {
		return partitioningRange;
	}

	public void setPartitioningRange(ArrayList<Integer> partitioningRange) {
		this.partitioningRange = partitioningRange;
	}
}
