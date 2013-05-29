package seep.comm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import seep.P;
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
		
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		return k;
	}
	
	public void run() {
		
		/** Experimental asyn **/
//		try{
//			//Get inputQueue from owner
//			DataStructureAdapter dsa = owner.getDSA();
//			//Get inputStream of incoming connection
//			InputStream is = upstreamSocket.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is, 8192);
//			Input i = new Input(bis);
//
//			while(goOn){
//				TuplePayload tp = k.readObject(i, TuplePayload.class);
//				DataTuple reg = new DataTuple(idxMapper, tp);
//				dsa.push(reg);
//			}
//		}
//		catch(IOException io){
//			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
//			io.printStackTrace();
//		}
		

		
		/** experimental sync **/
		try{
			//Get inputQueue from owner
			DataStructureAdapter dsa = owner.getDSA();
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			Input i = new Input(bis);
			BatchTuplePayload batchTuplePayload = null;

			while(goOn){
				batchTuplePayload = k.readObject(i, BatchTuplePayload.class);
				ArrayList<TuplePayload> batch = batchTuplePayload.batch;
				for(TuplePayload t_payload : batch){
					long incomingTs = t_payload.timestamp;
					owner.setTsData(incomingTs);
//System.out.println("new data ts: "+incomingTs);
					//Put data in inputQueue
					if(owner.checkSystemStatus()){
						DataTuple reg = new DataTuple(idxMapper, t_payload);
						dsa.push(reg);
					}
					else{
						System.out.println("trash in TCP buffers");
					}
				}
			}
			NodeManager.nLogger.severe("-> Data connection closing...");
			upstreamSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}