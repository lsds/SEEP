package seep.comm;

import java.io.Serializable;
import java.util.ArrayList;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.operator.EndPoint;
import seep.processingunit.PUContext;
import seep.runtimeengine.CommunicationChannel;
import seep.runtimeengine.OutputQueue;

/**
* Dispatcher. This is the class in charge of sending information to other operators
*/

public class Dispatcher implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	// opContext to have knowledge of downstream and upstream
	private PUContext puCtx;
	private OutputQueue outputQueue;
	
	//Assigned by Operator
	private Router router = null;

	private ArrayList<Integer> targets = new ArrayList<Integer>();
	
	public Dispatcher(PUContext puCtx, OutputQueue outputQueue){
		this.puCtx = puCtx;
		this.outputQueue = outputQueue;
	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public void setPUCtx(PUContext puCtx) {
		this.puCtx = puCtx;
	}
	
	//Start incoming data, one thread has finished replaying
	public synchronized void startIncomingData(){
		outputQueue.start();
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
			EndPoint dest = puCtx.getDownstreamTypeConnection().elementAt(target);
			outputQueue.sendToDownstream(dt, dest, false, false);
		}
		catch(ArrayIndexOutOfBoundsException aioobe){
			System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+puCtx.getDownstreamTypeConnection().size());
			aioobe.printStackTrace();
		}
	}
	
	public void sendData(DataTuple dt, int value, boolean now){
//		System.out.println("get targets: ");
		targets = router.forward(dt, value, now);
		for(Integer target : targets){
			try{
//			System.out.println("TARGET: "+target.toString());
				EndPoint dest = puCtx.getDownstreamTypeConnection().elementAt(target);
				outputQueue.sendToDownstream(dt, dest, now, false);
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+puCtx.getDownstreamTypeConnection().size());
				aioobe.printStackTrace();
			}
		}
	}
	
////	//When batch timeout expires, this method ticks every possible destination to update the clocks
	public void batchTimeOut(){
		DataTuple dt = null;
		for(EndPoint channelRecord : puCtx.getDownstreamTypeConnection()){
			if(channelRecord instanceof CommunicationChannel){
				//Tick with beacon for every destination, so that this can update their clocks
//				sendToDownstream(dt, channelRecord, false, true);
			}
		}
	}
}