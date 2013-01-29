package seep.runtimeengine;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import seep.comm.serialization.DataTuple;

public class Barrier implements DataStructureI {

	private int currentThreads = 0;
	private CyclicBarrier staticBarrier;
	private ArrayList<DataTuple> data = new ArrayList<DataTuple>();
	
	public Barrier(int initialNumberOfThreads){
		this.currentThreads = initialNumberOfThreads;
		staticBarrier = new CyclicBarrier(initialNumberOfThreads);
	}
	
	public void reconfigureBarrier(int numThreads){
//		System.out.println("In reconfigure barrier");
		currentThreads = numThreads;
//		System.out.println("gonna reset");
		staticBarrier.reset();
//		System.out.println("reset done");
		staticBarrier = new CyclicBarrier(numThreads);
//		System.out.println("created new barrier");
	}
	
	@Override
	public DataTuple pull() {
		return null;
	}
	
	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// Automatically it blocks...
		synchronized(this){
			try {
				this.wait();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Returns the data when it is notified that the barrier is done
//		System.out.println("I have been notified by the other guy to return the data");
		return data;
	}

	@Override
	public void push(DataTuple dt) {
		data.add(dt);
		try {
			int arrivalIdx = staticBarrier.await();
//			System.out.println("I PUSH A DATA TUPLE, with index: "+arrivalIdx);
			// If this is the last thread to arrive
			if(arrivalIdx == 0){
//				System.out.println("Since, I have reached the last, I notify the dataconsumer thread to go on...");
				// Then notify this object, so that data is return to application handler
				synchronized(this){
					this.notify();
				}
			}
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
