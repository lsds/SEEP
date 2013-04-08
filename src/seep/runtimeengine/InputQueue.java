package seep.runtimeengine;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import seep.P;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.MetricsReader;

public class InputQueue implements DataStructureI{

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
	
	public void clean(){
		try {
			MetricsReader.eventsInputQueue.dec();
			inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("INPUT QUEUE SIZE BEFORE CLEANING: "+MetricsReader.eventsInputQueue.getCount());
		System.out.println("BEFORE- REAL SIZE OF INPUT QUEUE: "+inputQueue.size());
		MetricsReader.eventsInputQueue.clear();
		inputQueue.clear();
		System.out.println("AFTER- REAL SIZE OF INPUT QUEUE: "+inputQueue.size());
		System.out.println("INPUT QUEUE SIZE AFTER CLEANING: "+MetricsReader.eventsInputQueue.getCount());
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}
}