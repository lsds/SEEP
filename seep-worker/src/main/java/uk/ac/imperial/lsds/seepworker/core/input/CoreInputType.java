package uk.ac.imperial.lsds.seepworker.core.input;

public enum CoreInputType {
	REMOTE((short)0), 
	LOCAL((short)1), 
	MASTER((short)2);
	
	private short type;
	
	CoreInputType(short type){
		this.type = type;
	}
	
	public short ofType(){
		return type;
	}
}
