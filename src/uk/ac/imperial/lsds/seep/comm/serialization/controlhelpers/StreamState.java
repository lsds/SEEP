package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

public class StreamState {
	
	private int targetOpId;
	
	public StreamState(){}
	
	public StreamState(int targetOpId){
		this.targetOpId = targetOpId;
	}
	
	public int getTargetOpId(){
		return targetOpId;
	}

}
