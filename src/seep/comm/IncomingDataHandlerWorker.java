package seep.comm;

import seep.infrastructure.NodeManager;
import seep.operator.*;
import seep.comm.tuples.*;

import java.io.*;
import java.net.*;

public class IncomingDataHandlerWorker implements Runnable{

	private int uid = 0;
	private Socket upstreamSocket = null;
	private Operator owner = null;
	private boolean goOn;
	
	public IncomingDataHandlerWorker(int uid, Socket upstreamSocket, Operator owner){
		//upstream id
		this.uid = uid;
		this.upstreamSocket = upstreamSocket;
		this.owner = owner;
		this.goOn = true;
	}
	
	public void run() {
		
		try{
			//Get inputQueue from owner
			InputQueue iq = owner.getInputQueue();
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			Seep.EventBatch batch = null;
			
//			int laps = 0;
//			long totalTime = 0;
			
			while(goOn){
//				long start = System.currentTimeMillis();
//				System.out.println("pre-check?");
//				if(!owner.getOperatorStatus().equals(Operator.OperatorStatus.INITIALISING_STATE)){
//					System.out.println("wait for read");
					batch = Seep.EventBatch.parseDelimitedFrom(is);
//					System.out.println("read !!");
				
					for(Seep.DataTuple dt : batch.getEventList()){
						long incomingTs = dt.getTs();
						owner.setTsData(incomingTs);
//						if(!owner.getOperatorStatus().equals(Operator.OperatorStatus.INITIALISING_STATE)){
							//owner.processData(dt);
						
						//Put data in inputQueue
						iq.push(dt);
						
						
//							if(owner.isOrderSensitive()){
//								iq.pushEvent(uid, dt);
//							}
//							else{
//								owner.processData(dt);
//							}
						}
//						else{
//							//Installing state, clean channel from remaining tuples in the batch
//							NodeManager.nLogger.info("-> DATA processing cleaned. INITIALISING_STATE");
//							batch = null;
//							break;
//						}
					
					}
//				}
//				else{
//					System.out.println("INSTALLING STATE +++++++++++++----------+++++++-------+++++-----+++++--------+");
//				}
//			}
//				totalTime += System.currentTimeMillis() - start;
//				laps++;
//				if(laps == 100){
//					laps = 0;
//					System.out.println("R: "+(totalTime/100));
//					totalTime = 0;
//				}
			
			System.out.println("ALERT !!!!!!");
			upstreamSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}
