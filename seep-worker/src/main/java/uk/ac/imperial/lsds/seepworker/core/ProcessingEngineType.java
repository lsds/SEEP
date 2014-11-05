package uk.ac.imperial.lsds.seepworker.core;

public enum ProcessingEngineType {
	SINGLE_THREAD((short)0);
	
	private short type;
	
	ProcessingEngineType(short type){
		this.type = type;
	}
	
	public short ofType(){
		return type;
	}
}
