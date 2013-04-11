package seep.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.messages.BatchTuplePayload;
import seep.comm.serialization.messages.Payload;
import seep.comm.serialization.messages.TuplePayload;
import seep.comm.serialization.serializers.ArrayListSerializer;
import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.DataStructureAdapter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class IncomingDataHandlerWorker implements Runnable{

	private int uid = 0;
	private Socket upstreamSocket = null;
	private CoreRE owner = null;
	private boolean goOn;
	private Map<String, Integer> idxMapper;
	private Kryo k = null;
	
	public IncomingDataHandlerWorker(int uid, Socket upstreamSocket, CoreRE owner, Map<String, Integer> idxMapper){
		//upstream id
		this.uid = uid;
		this.upstreamSocket = upstreamSocket;
		this.owner = owner;
		this.goOn = true;
		this.idxMapper = idxMapper;
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.setClassLoader(owner.getRuntimeClassLoader());
//		k.register(DataTuple.class);
//		k.register(Object.class);
		
		
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		
		
//		k.register(BatchTuplePayload.class);
//		k.register(TuplePayload.class);
//		k.register(Payload.class);
//		k.register(ArrayList.class, new ArrayListSerializer());
		
//		k.register(BatchDataTuple.class);
		return k;
	}
	
	public void run() {
		try{
			//Get inputQueue from owner
			DataStructureAdapter dsa = owner.getDSA();
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			Input i = new Input(is);
//			BatchDataTuple batchDataTuple = null;
			BatchTuplePayload batchTuplePayload = null;

			while(goOn){
//				batchDataTuple = k.readObject(i, BatchDataTuple.class);
//				batchTuplePayload = k.readObject(i, BatchTuplePayload.class);
				TuplePayload tp = k.readObject(i, TuplePayload.class);
System.out.println("OGT");
System.exit(0);
				DataTuple reg = new DataTuple(idxMapper, tp);
				dsa.push(reg);
			}
//			
////				ArrayList<DataTuple> batch = batchDataTuple.getTuples();
//				ArrayList<TuplePayload> batch = batchTuplePayload.batch;
////				for(DataTuple datatuple : batch){
//				for(TuplePayload t_payload : batch){
//					//long incomingTs = datatuple.getTimestamp();
//					long incomingTs = t_payload.timestamp;
//					owner.setTsData(incomingTs);
//					//Put data in inputQueue
//					if(owner.checkSystemStatus()){
//						//dsa.push(datatuple);
////						System.out.println("PRE-SET: "+reg.size());
//						DataTuple reg = new DataTuple(idxMapper, t_payload);
////						System.out.println("POST-SET: "+reg.size());
//						//reg.set(t_payload);
////						System.out.println("Forwarding DT with size: "+reg.size());
//						dsa.push(reg);
//						//dsa.push(new DataTuple(idxMapper ,t_payload));
//					}
//					else{
//						System.out.println("trash in TCP buffers");
//					}
//				}
//			}
			NodeManager.nLogger.severe("-> Data connection closing...");
			upstreamSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}