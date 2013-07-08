package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

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
