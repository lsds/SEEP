package seep.runtimeengine;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;

import seep.comm.serialization.DataTuple;

public class Barrier implements DataStructureI {

	private CyclicBarrier staticBarrier;
	private ArrayList<DataTuple> data = new ArrayList<DataTuple>();
	private DeliverWorker dw;
	
	private BlockingQueue<ArrayList<DataTuple>> sbq = new SynchronousQueue<ArrayList<DataTuple>>();
	
	public Barrier(int initialNumberOfThreads){
		dw = new DeliverWorker();
		staticBarrier = new CyclicBarrier(initialNumberOfThreads, dw);
	}
	
	public void reconfigureBarrier(int numThreads){
		System.out.println("Reseting barrier");
//		data.clear();
		synchronized(staticBarrier){
			int waiting = staticBarrier.getNumberWaiting();
			staticBarrier.reset();
			System.out.println("Barrier is now reset, there were "+waiting+" threads waiting");
			staticBarrier = null;
			staticBarrier = new CyclicBarrier(numThreads, dw);
			System.out.println("And new barrier created with: "+numThreads);
		}
	}
	
	@Override
	public DataTuple pull() {
		return null;
	}

	public ArrayList<DataTuple> pull_from_barrier(){
		ArrayList<DataTuple> toRet = null;
		try {
//System.out.println("PULL- waiting to take");
			toRet =  sbq.take();
//System.out.println("PULL- took");
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toRet;
	}

	@Override
	public void push(DataTuple dt) {
		// We put the data
		synchronized(data){
			data.add(dt);
		}
		// And wait on the barrier
		try {
//System.out.println("PUSH- waiting in the barrier");
			staticBarrier.await();
//System.out.println("PUSH- barrier crossed");
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (BrokenBarrierException e) {
			staticBarrier.reset();
			e.printStackTrace();
		}
	}
	
	public class DeliverWorker implements Runnable{
		public void run(){
			ArrayList<DataTuple> copy = new ArrayList<DataTuple>(data);
			data.clear();
			try {
//System.out.println("PUSH- Waiting to put");
				sbq.put(copy);
//System.out.println("PUSH- put");
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}