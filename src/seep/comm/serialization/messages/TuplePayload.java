package seep.comm.serialization.messages;

import java.io.Serializable;

public class TuplePayload implements Serializable{
	
	public static final long serialVersionUID = 1L;
	public long timestamp;
	public int schemaId;
	public Payload attrValues;
	
	public TuplePayload(){
		
	}
	
}
