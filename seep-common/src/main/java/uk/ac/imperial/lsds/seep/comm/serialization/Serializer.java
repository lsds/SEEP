package uk.ac.imperial.lsds.seep.comm.serialization;

public interface Serializer {

	public byte[] serialize(Object object);
	public Object deserialize(byte[] data);
	
}