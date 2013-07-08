package uk.co.imperial.lsds.seep.comm.serialization;

import com.esotericsoftware.kryo.Kryo;


public class KryoSerializer<T> implements SeePSerializer<T> {
	
	public Kryo k = new Kryo();
	
	@Override
	public T deserialize(byte[] data) throws SeePSerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] serialize(T data) throws SeePSerializationException {
		// TODO Auto-generated method stub
		return null;
	}

}
