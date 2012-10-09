package seep.operator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import seep.Main;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.MetricsReader;

public class InputQueue {

	private BlockingQueue<DataTuple> inputQueue;
	
	public InputQueue(){
		inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(Main.valueFor("inputQueueLength")));
	}
	
	public InputQueue(int size){
		inputQueue = new ArrayBlockingQueue<DataTuple>(size);
	}
	
	public synchronized void push(DataTuple data){
		try {
			inputQueue.put(data);
			MetricsReader.eventsInputQueue.inc();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DataTuple pull(){
		try {
			MetricsReader.eventsInputQueue.dec();
			return inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

//
//package seep.comm;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.Map;
//
//import seep.comm.tuples.Seep;
//import seep.comm.tuples.Seep.DataTuple;
//import seep.operator.Operator;
//
//public class InputQueue2 {
//
//	private int lastPopInput = 0;
//	private Map<Integer, Deque<Seep.DataTuple>> inputStreams = new HashMap<Integer, Deque<Seep.DataTuple>>();
//	private ArrayList<Deque<Seep.DataTuple>> inputs = new ArrayList<Deque<Seep.DataTuple>>();
//	private Operator owner = null;
//	
//	public InputQueue2(Operator owner){
//		this.owner = owner;
//	}
//
//	public synchronized void pushEvent(int uid, DataTuple dt) {
//		//If it is a new stream we initialise it
//		synchronized(inputStreams){
//			synchronized(inputs){
//				long timestamp = dt.getTs();
//				if(!inputStreams.containsKey(uid)){
//					ArrayDeque<Seep.DataTuple> d = new ArrayDeque<Seep.DataTuple>();
//					inputStreams.put(uid, d);
//					inputs.add(d);
//				}
//				//Push event in the queue
//				inputStreams.get(uid).push(dt);
//				dt = popOlderEvent(timestamp);
//				owner.processData(dt);
//			}
//		}
//	}
//	
//	/// \todo{refactor to a CIRCULAR BUFFER to do this with one buffer}
//	private Seep.DataTuple popOlderEvent(long timestamp){
//		long lessTs = Long.MAX_VALUE;
//		int pointer = 0;
//		Seep.DataTuple dt = null;
////		for(int i = lastPopInput; i<inputs.size(); i++){
//		for(int i = 0; i<inputs.size(); i++){
//			dt = inputs.get(i).peek();
//			if(dt != null){
//				if(dt.getTs() < lessTs){
//					lessTs = dt.getTs();
//					pointer = i;
//				}
//			}
//		}
////		if(lastPopInput != 0){
////			for(int i = 0; i<lastPopInput-1; i++){
////				dt = inputs.get(i).peek();
////				if(dt != null){
////					if(dt.getTs() < lessTs){
////						lessTs = dt.getTs();
////						pointer = i;
////					}
////				}
////			}
////		}
////		lastPopInput = pointer;
//		dt = inputs.get(pointer).poll();
//		return dt;
//	}
//}
