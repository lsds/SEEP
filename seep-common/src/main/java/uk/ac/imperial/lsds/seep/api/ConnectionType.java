package uk.ac.imperial.lsds.seep.api;

public enum ConnectionType {
	
	ONE_AT_A_TIME((short)0), 
	BATCH((short)1), 
	WINDOW((short)2), 
	ORDERED((short)3),
	UPSTREAM_SYNC_BARRIER((short)4);
	
	private short type;
	
	ConnectionType(short type){
		this.type = type;
	}
	
	public short ofType(){
		return type;
	}
}