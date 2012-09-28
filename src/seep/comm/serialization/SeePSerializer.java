package seep.comm.serialization;

/* SeePSerializer is a tentative generic solution that defines serialize and deserialize making serialization/deserialization solutions independent. However,
 * there are several issues in this attempt, and this is an ongoing test. */

public interface SeePSerializer<T> {

	public byte[] serialize(T data) throws SeePSerializationException;
	
	public T deserialize(byte[] data) throws SeePSerializationException;
	
}
