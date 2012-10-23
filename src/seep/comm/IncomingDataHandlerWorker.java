package seep.comm;

import seep.infrastructure.NodeManager;
import seep.operator.*;
import seep.comm.serialization.BatchDataTuple;
import seep.comm.serialization.DataTuple;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class IncomingDataHandlerWorker implements Runnable{

	private int uid = 0;
	private Socket upstreamSocket = null;
	private Operator owner = null;
	private boolean goOn;
	private Kryo k = null;
	
	public IncomingDataHandlerWorker(int uid, Socket upstreamSocket, Operator owner){
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
		k.register(DataTuple.class);
		k.register(BatchDataTuple.class);
		return k;
	}
	
public void run() {
		try{
			//Get inputQueue from owner
			InputQueue iq = owner.getInputQueue();
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			Input i = new Input(is);
			BatchDataTuple batchDataTuple = null;

			while(goOn){
//				System.out.println("Ready to read: ");
				batchDataTuple = k.readObject(i, BatchDataTuple.class);
//				int size = i.total();
//				i.rewind();
//				System.out.println("rx: "+size);
				ArrayList<DataTuple> batch = batchDataTuple.getTuples();
				for(DataTuple datatuple : batch){
					long incomingTs = datatuple.getTs();
					owner.setTsData(incomingTs);
					//Put data in inputQueue
					//HACK FOR EXPERIMENT
					if(owner.getClass().toString().equals("class seep.operator.collection.mapreduceexample.Map")){
						iq.pushOrShed(datatuple);
					}
					else{
						iq.push(datatuple);
					}
				}
			}
			System.out.println("ALERT !!!!!!");
			upstreamSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
	
//	public void run() {
//		
//		try{
//			//Get inputQueue from owner
//			InputQueue iq = owner.getInputQueue();
//			//Get inputStream of incoming connection
//			InputStream is = upstreamSocket.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is);
//			Input i = new Input(bis);
//			DataTuple datatuple = null;
////			Seep.EventBatch batch = null;
//			
////			int laps = 0;
////			long totalTime = 0;
//			
//			while(goOn){
////				long start = System.currentTimeMillis();
////				System.out.println("pre-check?");
////				if(!owner.getOperatorStatus().equals(Operator.OperatorStatus.INITIALISING_STATE)){
////					System.out.println("wait for read");
////				batch = Seep.EventBatch.parseDelimitedFrom(is);
//				datatuple = k.readObject(i, DataTuple.class);
////					System.out.println("read !!");
//				
////				for(Seep.DataTuple dt : batch.getEventList()){
//				long incomingTs = datatuple.getTs();
//				owner.setTsData(incomingTs);
////						if(!owner.getOperatorStatus().equals(Operator.OperatorStatus.INITIALISING_STATE)){
//							//owner.processData(dt);
//						
//						//Put data in inputQueue
//				iq.push(datatuple);
//						
//						
////							if(owner.isOrderSensitive()){
////								iq.pushEvent(uid, dt);
////							}
////							else{
////								owner.processData(dt);
////							}
////				}
////						else{
////							//Installing state, clean channel from remaining tuples in the batch
////							NodeManager.nLogger.info("-> DATA processing cleaned. INITIALISING_STATE");
////							batch = null;
////							break;
////						}
//					
//			}
////				}
////				else{
////					System.out.println("INSTALLING STATE +++++++++++++----------+++++++-------+++++-----+++++--------+");
////				}
////			}
////				totalTime += System.currentTimeMillis() - start;
////				laps++;
////				if(laps == 100){
////					laps = 0;
////					System.out.println("R: "+(totalTime/100));
////					totalTime = 0;
////				}
//			
//			System.out.println("ALERT !!!!!!");
//			upstreamSocket.close();
//		}
//		catch(IOException io){
//			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
//			io.printStackTrace();
//		}
//	}
}
