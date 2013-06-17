package seep.comm.serialization.controlhelpers;

import seep.operator.State;

public class StateChunk {

	private int opId;
	private int partitionNumber;
	private int sequenceNumber;
	private int totalChunks;
	private State state;
	
	public StateChunk(){}
	
	public StateChunk(int opId, int partitionNumber, int sequenceNumber, int totalChunks, State state){
		this.opId = opId;
		this.partitionNumber = partitionNumber;
		this.sequenceNumber = sequenceNumber;
		this.totalChunks = totalChunks;
		this.state = state;
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
}
