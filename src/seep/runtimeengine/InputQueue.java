package seep.runtimeengine;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import seep.P;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.MetricsReader;

public class InputQueue {

	private BlockingQueue<DataTuple> inputQueue;
	
	public InputQueue(){
		inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(P.valueFor("inputQueueLength")));
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
	
	public synchronized boolean pushOrShed(DataTuple data){
		boolean inserted = inputQueue.offer(data);
		if (inserted) MetricsReader.eventsInputQueue.inc();
		return inserted;
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