package seep.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import seep.comm.serialization.BatchDataTuple;
import seep.comm.serialization.DataTuple;
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
	private Kryo k = null;
	
	public IncomingDataHandlerWorker(int uid, Socket upstreamSocket, CoreRE owner){
		//upstream id
		this.uid = uid;
		this.upstreamSocket = upstreamSocket;
		this.owner = owner;
		this.goOn = true;
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.setClassLoader(owner.getRuntimeClassLoader());
		k.register(DataTuple.class);
		k.register(BatchDataTuple.class);
		return k;
	}
	
	public void run() {
		try{
			//Get inputQueue from owner
//			InputQueue iq = owner.getInputQueue();
			DataStructureAdapter dsa = owner.getDSA();
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			Input i = new Input(is);
			BatchDataTuple batchDataTuple = null;

			while(goOn){
				batchDataTuple = k.readObject(i, BatchDataTuple.class);
				ArrayList<DataTuple> batch = batchDataTuple.getTuples();
				for(DataTuple datatuple : batch){
					long incomingTs = datatuple.getTimestamp();
					owner.setTsData(incomingTs);
					//Put data in inputQueue
					if(owner.checkSystemStatus()){
						dsa.push(datatuple);
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