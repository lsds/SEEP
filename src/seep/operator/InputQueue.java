package seep.operator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import seep.comm.tuples.Seep;

public class InputQueue {

	private BlockingQueue<Seep.DataTuple> inputQueue;
	
	public InputQueue(){
		inputQueue = new LinkedBlockingQueue<Seep.DataTuple>();
	}
	
	public void push(Seep.DataTuple data){
		try {
			inputQueue.put(data);
//			System.out.println("ID pushed: "+inputQueue.size());
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Seep.DataTuple pull(){
		try {
//			System.out.println("ID pop:");
			return inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
