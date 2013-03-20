package seep.runtimeengine;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.SynchronousQueue;

import seep.comm.serialization.DataTuple;

/** 
 * Reason why the barrier is implemented with the Phaser of Java 7 instead of a common CyclicBarrier of previous java is because of its robust support
 * for dynamicity. Only this feature justified changing the version of the language. 
 * **/

public class Barrier implements DataStructureI {

	private Phaser staticBarrier;
	
	private ArrayList<DataTuple> data = new ArrayList<DataTuple>();
	
	private BlockingQueue<ArrayList<DataTuple>> sbq = new SynchronousQueue<ArrayList<DataTuple>>();
	
	public Barrier(int initialNumberOfThreads){
		staticBarrier = new Phaser(initialNumberOfThreads){
			protected boolean onAdvance(int phase, int parties) {
				ArrayList<DataTuple> copy = new ArrayList<DataTuple>(data);
				data.clear();
				try {
					sbq.put(copy);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				return false;
			}
		};
	}
	
	public void reconfigureBarrier(int numThreads){
		// Dynamic tiering of the hierarchical phaser
		// Check the num of Threads. 
		// If it hits a given threshold (static or dynamic) 
		// partition the parties into two phasers
		// register those in the master one
		staticBarrier.register();
	}
	
	@Override
	public DataTuple pull() {
		return null;
	}

	public ArrayList<DataTuple> pull_from_barrier(){
		ArrayList<DataTuple> toRet = null;
		try {
			toRet =  sbq.take();
		} 
		catch (InterruptedException e) {
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
		staticBarrier.arriveAndAwaitAdvance();
	}
}