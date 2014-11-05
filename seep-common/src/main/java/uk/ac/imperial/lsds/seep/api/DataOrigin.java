package uk.ac.imperial.lsds.seep.api;

public enum DataOrigin {
	
	NETWORK((short)0), 
	FILE((short)1),
	IPC((short)2), // ??
	RDD((short)3),
	KAFKA((short)4),
	HDFS((short)5);
	
	private short type;
	
	DataOrigin(short type){
		this.type = type;
	}
	
	public short ofType(){
		return type;
	}
}
