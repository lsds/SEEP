package uk.ac.imperial.lsds.seep.comm;

import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.protocol.ProtocolAPI;

import com.esotericsoftware.kryo.Kryo;

public class KryoFactory {

	public static Kryo buildKryo(){
		return new Kryo();
	}
	
	public static Kryo buildKryoWithRegistration(Set<Object> classToRegister){
		Kryo k = new Kryo();
		int registrationId = 0;
		for(Object o : classToRegister){
			k.register(o.getClass(), registrationId);
			registrationId++;
		}
		return k;
	}
	
	public static Kryo buildKryoWithRegistration(Set<Object> classToRegister, ClassLoader cl){
		Kryo k = new Kryo();
		int registrationId = 0;
		for(Object o : classToRegister){
			k.register(o.getClass(), registrationId);
			registrationId++;
		}
		k.setClassLoader(cl);
		return k;
	}
	
	public static Kryo buildKryoForMasterWorkerProtocol(){
		Kryo k = new Kryo();
		for(ProtocolAPI command : ProtocolAPI.values()){
			k.register(command.getClass(), command.type());
		}
		return k;
	}
	
	public static Kryo buildKryoForMasterWorkerProtocol(ClassLoader cl){
		Kryo k = new Kryo();
		for(ProtocolAPI command : ProtocolAPI.values()){
			k.register(command.getClass(), command.type());
		}
		k.setClassLoader(cl);
		return k;
	}
	
}
