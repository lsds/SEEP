package uk.ac.imperial.lsds.seepworker.core.input;

public enum InputAdapterReturnType {
	ONE((short)0), 
	MANY((short)1);
	
	private short type;
	
	InputAdapterReturnType(short type){
		this.type = type;
	}
	
	public short ofType(){
		return type;
	}
}
