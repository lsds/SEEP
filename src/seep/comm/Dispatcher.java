package seep.comm;

import seep.comm.routing.Router;
import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.*;

import java.util.*;
import java.io.*;
import java.net.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

/**
* Dispatcher. This is the class in charge of sending information to other operators
*/

public class Dispatcher implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Kryo k;
	
	// opContext to have knowledge of downstream and upstream
	private OperatorContext opContext;
	private OutputQueue outputQueue;
	
	//Assigned by Operator
	private Router router = null;

	private ArrayList<Integer> targets = new ArrayList<Integer>();
	
	/** btnck detector vars **/
	int laps = 0;
	long elapsed = 0;
	
	public Dispatcher(OperatorContext opContext, OutputQueue outputQueue){
		this.opContext = opContext;
		this.outputQueue = outputQueue;
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		k = new Kryo();
		k.register(ControlTuple.class);
		return k;
	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public void setOpContext(OperatorContext opContext) {
		this.opContext = opContext;
	}
	
	//Start incoming data, one thread has finished replaying
	public synchronized void startIncomingData(){
		outputQueue.start();
	}

	public synchronized void stopConnection(int opID) {
		//Stop incoming data, a new thread is replaying
//		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore increments: "+replaySemaphore.toString());
		
		/**
		 * hack done on july the third 2012 to get parallel recovery results.
		 *  we make sure that conn is only stop once
		 */
//if (replaySemaphore.get() > 0){
//	return;
//}
		
//		replaySemaphore.incrementAndGet();
		outputQueue.stop();
		opContext.getCCIfromOpId(opID, "d").getStop().set(true);
	}
	
	int remainingWindow = 0;
	int splitWindow = 0;
	int target = 0;
	int downstreamIndex = 0;
	int numberOfDownstreams = 0;
	
	public void h_sendData(DataTuple dt){
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
			target = downstreamIndex++%numberOfDownstreams;
			// get the real index from the virtual one.
			//target = virtualIndexToRealIndex.get(target);
		}
		remainingWindow--;
		/// \todo Return the real Index, got from the virtual one. Optimize this
		//target = virtualIndexToRealIndex.get(target);
		try{
//		System.out.println("TARGET: "+target.toString());
			Object dest = opContext.getDownstreamTypeConnection().elementAt(target);
			outputQueue.sendToDownstream(dt, dest, false, false);
		}
		catch(ArrayIndexOutOfBoundsException aioobe){
			System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+opContext.getDownstreamTypeConnection().size());
			aioobe.printStackTrace();
		}
	}
	
	public void sendData(DataTuple dt, int value, boolean now){
//		System.out.println("get targets: ");
		targets = router.forward(dt, value, now);
		for(Integer target : targets){
			try{
//			System.out.println("TARGET: "+target.toString());
				Object dest = opContext.getDownstreamTypeConnection().elementAt(target);
				outputQueue.sendToDownstream(dt, dest, now, false);
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+opContext.getDownstreamTypeConnection().size());
				aioobe.printStackTrace();
			}
		}
	}
	
	//When batch timeout expires, this method ticks every possible destination to update the clocks
	public void batchTimeOut(){
		DataTuple dt = null;
		for(Object channelRecord : opContext.getDownstreamTypeConnection()){
			if(channelRecord instanceof CommunicationChannel){
				//Tick with beacon for every destination, so that this can update their clocks
//				sendToDownstream(dt, channelRecord, false, true);
			}
		}
	}

	public void sendAllUpstreams(ControlTuple ct){
		for(int i = 0; i < opContext.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i);
		}		
	}
	
	public void sendMinUpstream(ControlTuple ct) {
		int index = opContext.minimumUpstream().index();
		sendUpstream(ct, index);
	}

	public void sendUpstream(ControlTuple ct, int index){
		Object obj = (Object)opContext.getUpstreamTypeConnection().elementAt(index);
		if(obj instanceof Operator){
			Operator operatorObj = (Operator) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).getDownstreamControlSocket();
			Output output = null;
			try{
				output = new Output(socket.getOutputStream());
				synchronized (output){
//					tuple.writeDelimitedTo(socket.getOutputStream());
					k.writeObject(output, ct);
					output.flush();
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void sendDownstream(ControlTuple ct, int index){
		Object obj = (Object)opContext.getDownstreamTypeConnection().elementAt(index);
		if(obj instanceof Operator){
			Operator operatorObj = (Operator) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).getDownstreamControlSocket();
			Output output = null;
			try{
				output = new Output(socket.getOutputStream());
				synchronized (socket){
//					tuple.writeDelimitedTo(socket.getOutputStream());
					k.writeObject(output, ct);
					output.flush();
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void ackControlMessage(ControlTuple genericAck, OutputStream os){
		Output output = new Output(os);
		k.writeObject(output, genericAck);
		output.flush();
	}
	
	public void initStateMessage(ControlTuple initStateMsg, OutputStream os){
		Output output = new Output(os);
		k.writeObject(output, initStateMsg);
		output.flush();
	}
}