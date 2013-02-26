package seep.comm.serialization;

import java.io.Serializable;

public abstract class RootTuple implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int tupleSchemaId;
	private long timestamp;
	private int id;
	
	public int getTupleSchemaId() {
		return tupleSchemaId;
	}
	public void setTupleSchemaId(int tupleSchemaId) {
		this.tupleSchemaId = tupleSchemaId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public RootTuple(){
		
	}
	
	public RootTuple(int tupleSchemaId, long timestamp, int id){
		this.tupleSchemaId = tupleSchemaId;
		this.timestamp = timestamp;
		this.id = id;
	}
	
}
