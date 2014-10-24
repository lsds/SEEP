package uk.ac.imperial.lsds.seep.comm.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerializer implements Serializer{

	private ByteArrayOutputStream baos;
	private ByteArrayInputStream bais;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public JavaSerializer(){
		// TODO: configure buffer sizes, etc
		baos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public byte[] serialize(Object object) {
		try {
			oos.writeObject(object);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	@Override
	public Object deserialize(byte[] data) {
		Object o = null;
		bais = new ByteArrayInputStream(data);
		try{
			ois = new ObjectInputStream(bais);
			o = ois.readObject();
		}
		catch(IOException io){
			io.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}

}
