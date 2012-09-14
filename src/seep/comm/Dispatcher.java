package seep.comm;

import seep.Main;
import seep.buffer.Buffer;
import seep.comm.routing.LoadBalancerI;
import seep.comm.routing.Router;
import seep.comm.routing.StatelessDynamicLoadBalancer;
import seep.comm.tuples.*;
import seep.comm.tuples.Seep.ControlTuple;
import seep.infrastructure.NodeManager;
import seep.operator.*;
import seep.utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.net.*;

/**
* Dispatcher. This is the class in charge of sending information to other operators
*/

@SuppressWarnings("serial")
public class Dispatcher implements Serializable{
	
	// opContext to have knowledge of downstream and upstream
	private OperatorContext opContext;
	private OutputQueue outputQueue;
	
	// dispatchPolicy defines how it needs to deliver the information
	private DispatchPolicy dispatchPolicy = DispatchPolicy.ALL;
	
	/** THIS WILL DISSAPEAR **/
	// dispatcherFilter defines an optional filter for the dispatchPolicy.
	private LoadBalancerI loadBalancer = null;
	
	//Assigned by Operator
	private Router router = null;

	//private int filterValue = 0;
	//private int target = 0;
	private ArrayList<Integer> targets = new ArrayList<Integer>();
	
//	// replaySemaphore controls whether it is possible to send or not
//	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	
	/** btnck detector vars **/
	int laps = 0;
	long elapsed = 0;
	
	public enum DispatchPolicy {
		ALL, ANY, CONTENT_BASED
	}
	
	public Dispatcher(OperatorContext opContext, DispatchPolicy dispatchPolicy, LoadBalancerI loadBalancer, OutputQueue outputQueue){
		this.opContext = opContext;
		this.dispatchPolicy = dispatchPolicy;
		this.loadBalancer = loadBalancer;
		this.outputQueue = outputQueue;
//		if(this.dispatchPolicy == DispatchPolicy.CONTENT_BASED){
//			((ContentBasedFilter)loadBalancer).configureLoadBalancers(opContext);
//		}
	}
	
//	/// \todo {consider if this method can be avoided by implementing that line in other place}
//	public void startFilters(){
//		//If it is a contentBasedFilter, initialize the content-based filter and the chained filters if any
//		if (loadBalancer instanceof ContentBasedFilter){
//			((ContentBasedFilter)loadBalancer).initializeFilters();
//		}
//	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public void setOpContext(OperatorContext opContext) {
		this.opContext = opContext;
	}
	
	public DispatchPolicy getDispatchPolicy() {
		return dispatchPolicy;
	}

	public void setDispatchPolicy(DispatchPolicy t) {
		dispatchPolicy = t;
	}
	
	public LoadBalancerI getDispatcherFilter() {
		return loadBalancer;
	}
	
	public void setDispatchPolicy(DispatchPolicy t, LoadBalancerI df){
		dispatchPolicy = t;
		loadBalancer = df;
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
	
	public void sendData(Seep.DataTuple dt, int value, boolean now){
		targets = router.forward(dt, value, now);
		for(Integer target : targets){
			Object dest = opContext.getDownstreamTypeConnection().elementAt(target);
			outputQueue.sendToDownstream(dt, dest, now, false);
		}
	}
	
	
//	//dt is the tuple to send, value is a value provided by the user for content-based stuff. now specifies whether this tuples needs to be sent now or can be batched.
//	//public void sendData(Seep.DataTuple dt, int value, boolean now){
//	public void sendData(Seep.DataTuple dt, int value, boolean now){
//		if (dispatchPolicy == DispatchPolicy.ALL){
//			for(int i = 0; i < opContext.getDownstreamTypeConnection().size(); i++){
//				Object dest = opContext.getDownstreamTypeConnection().elementAt(i);
//				outputQueue.sendToDownstream(dt, dest, now, false);
//			}
//		}
//		else if (dispatchPolicy == DispatchPolicy.ANY) {
//			//if downstream is stateless
//			int target = ((StatelessDynamicLoadBalancer)loadBalancer).route();
//			Object dest = opContext.getDownstreamTypeConnection().elementAt(target);
//			outputQueue.sendToDownstream(dt, dest, now, false);
//		}
//		else if (dispatchPolicy == DispatchPolicy.CONTENT_BASED) {
//			targets = ((ContentBasedFilter)loadBalancer).applyFilter(dt, value);
//			for(Integer target : targets){
//				Object dest = opContext.getDownstreamTypeConnection().elementAt(target);
//				outputQueue.sendToDownstream(dt, dest, now, false);
//			}
//		}
//	}
	
	//When batch timeout expires, this method ticks every possible destination to update the clocks
	public void batchTimeOut(){
		Seep.DataTuple dt = null;
		for(Object channelRecord : opContext.getDownstreamTypeConnection()){
			if(channelRecord instanceof CommunicationChannel){
				//Tick with beacon for every destination, so that this can update their clocks
//				sendToDownstream(dt, channelRecord, false, true);
			}
		}
	}

	public void sendAllUpstreams(Seep.ControlTuple.Builder ct){
		for(int i = 0; i < opContext.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i);
		}		
	}
	
	public void sendMinUpstream(ControlTuple.Builder ct) {
		int index = opContext.minimumUpstream().index();
		sendUpstream(ct, index);
	}

	public void sendUpstream(Seep.ControlTuple.Builder ct, int index){
		Object obj = (Object)opContext.getUpstreamTypeConnection().elementAt(index);
		if(obj instanceof Operator){
			Operator operatorObj = (Operator) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).downstreamControlSocket;
			try{
				Seep.ControlTuple tuple = ct.build();
				synchronized (socket){
					tuple.writeDelimitedTo(socket.getOutputStream());
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void sendDownstream(Seep.ControlTuple.Builder ct, int index){
		Object obj = (Object)opContext.getDownstreamTypeConnection().elementAt(index);
		if(obj instanceof Operator){
			Operator operatorObj = (Operator) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).downstreamControlSocket;
			try{
				Seep.ControlTuple tuple = ct.build();
				synchronized (socket){
					tuple.writeDelimitedTo(socket.getOutputStream());
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
}

//private void sendToDownstream(Seep.DataTuple tuple, Object dest, boolean now, boolean beacon) {
////*&^*&^*&^*&^*&^
////BufferedOutputStream out = null;
//if(dest instanceof CommunicationChannelInformation){
//	CommunicationChannelInformation channelRecord = (CommunicationChannelInformation) dest;
///// \fixme {remove all this sync block}
//	synchronized(channelRecord){
//		Socket sock = channelRecord.downstreamSocketD;
//		Buffer buffer = channelRecord.buffer;
//		try{
//			//To send tuple
//			//if(channelRecord.replay.get()){
//			if(channelRecord.replay.compareAndSet(true, false)){
//				channelRecord.buffer.replay(channelRecord);
//				channelRecord.replay.set(false);
//				channelRecord.stop.set(false);
//				sock = channelRecord.downstreamSocketD;
//				//out = new BufferedOutputStream(sock.getOutputStream());
//				//At this point, this operator has finished replaying the tuples
//				NodeManager.setSystemStable();
//			}
//			if(!channelRecord.stop.get()){
//			
//				if(!beacon){
////					synchronized(channelRecord.batch){
//					channelRecord.batch.addEvent(tuple);
////				}
////System.out.println("tupleSize: "+tuple.getSerializedSize());
//					channelRecord.channelBatchSize--;//limitBatch--;
//				//TODO do i need to update also when I replay? (which mean that last_ts can
//				//also go backward)
//					channelRecord.last_ts = tuple.getTs();
//				}
//			
//			//If it is mandated to send the tuple now (URGENT), then channelBatchSize is put to 0
//				if(now) channelRecord.channelBatchSize = 0;
//				long currentTime = System.currentTimeMillis();
//			
//			/// \todo{Add the following line for include the batch timing mechanism}
////			if(channelRecord.channelBatchSize == 0 || (currentTime - channelRecord.tick) > ExecutionConfiguration.maxLatencyAllowed ){
//				if(channelRecord.channelBatchSize == 0){
//					Seep.EventBatch msg = channelRecord.batch.build();
//
//					channelRecord.tick = currentTime;
////					long start = System.currentTimeMillis();
////					System.out.println("*");
//					
///// \fixme {SYNCRHONIZE OVER THIS SOCKET IS DONE IN MANY PLACES (3), this becomes problematic in event of failure. Necessary to reduce SYNCH}							
//					
//					synchronized(sock){
////						System.out.println("@");
//						//msg.writeDelimitedTo(sock.getOutputStream());
//						msg.writeDelimitedTo(sock.getOutputStream());
////						int lessT = msg.getEvent(0).getTime();
////						int moreT = msg.getEvent(msg.getEventCount()-1).getTime();
////if((moreT-lessT) > 1){
////System.out.println("DIF: "+(moreT-lessT));
////}
//					}
////					elapsed += System.currentTimeMillis() -start;
////					laps++;
////					if(laps == 100){
////						System.out.println("sendOUT: "+elapsed/100);
////						laps = 0;
////						elapsed = 0;
////					}
//					channelRecord.batch = channelRecord.batch.clear();
//					int limit = Integer.parseInt(Main.valueFor("batchLimit"));
//					channelRecord.channelBatchSize = limit;
//				
//				//buffering batch
//				
//				buffer.save(msg);
//				
//				}
//			}
//			else if (!beacon){
//				//Is there any thread replaying?
//				while(replaySemaphore.get() >= 1){
//					//If so, wait.
//					synchronized(this){
//						this.wait();
//					}
//				}
//			}
//		}
//		catch(IOException io){
//			NodeManager.nLogger.severe("-> Dispatcher. While sending: "+io.getMessage());
//			io.printStackTrace();
////		System.exit(-1);
//		}
//		catch(InterruptedException ie){
//			NodeManager.nLogger.severe("-> Dispatcher. While trying to do wait() "+ie.getMessage());
//			ie.printStackTrace();
//		}
//		catch(Exception gen){
//			System.out.println("###EXCEPTION: "+gen.getMessage());
//			gen.printStackTrace();
//			System.out.println("channelBatchSize == 0???? : "+channelRecord.channelBatchSize);
//			System.out.println("channelRecord BATCH: "+channelRecord.batch.getEventCount());
//			System.out.println("TUPLE: "+tuple);
//			System.out.println("###");
//			System.exit(0);
//		}
//	
//	}//synch
//	
//}
//else if(dest instanceof Operator){
//	Operator operatorObj = (Operator) dest;
//	operatorObj.processData(tuple);
//}
//}