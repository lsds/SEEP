/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.SynchronousQueue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;

/** 
 * Reason why the barrier is implemented with the Phaser of Java 7 instead of a common CyclicBarrier of previous java is because of its robust support
 * for dynamicity. Only this feature justified changing the version of the language. 
 * **/

public class Barrier implements DataStructureI {

	private Phaser staticBarrier;

	private ArrayList<DataTuple> data = new ArrayList<DataTuple>();

	private BlockingQueue<ArrayList<DataTuple>> sbq = new SynchronousQueue<ArrayList<DataTuple>>();

	private long lastTimestamp = 0;
	private int repetitions = 0;
	private int repetitionsANN = 0 ;
	private long cummulatedTime = 0;
	private long cummulatedBarrierTime = 0 ; 
	private long barrierTimeEachPhase;

	public Barrier(int initialNumberOfThreads){
		staticBarrier = new Phaser(initialNumberOfThreads){
			protected boolean onAdvance(int phase, int parties) {

				cummulatedBarrierTime += (System.nanoTime() - barrierTimeEachPhase);
				long now = System.currentTimeMillis();

				if(lastTimestamp != 0){

					cummulatedTime += (now-lastTimestamp);
					lastTimestamp = now;
					repetitions++;
					repetitionsANN++;

					if(repetitions == 5000){
						System.out.println("AVG barrier time: "+(cummulatedTime)+" ms");
						repetitions = 0;
						cummulatedTime = 0;
					}

					if(repetitionsANN == 9500){
						System.out.println("Accum barrier time: "+((double)(cummulatedBarrierTime/1000000000.0))+" s");
						System.out.println("repetitions = " + repetitionsANN);
						repetitionsANN = 0 ; 
						cummulatedBarrierTime = 0 ;
					}
				}
				else{
					lastTimestamp = now;
				}
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
		//		staticBarrier.bulkRegister(numThreads);
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

			if( (data.size() == 1) ){
				barrierTimeEachPhase = System.nanoTime();
			}
		}
		try{
			// And wait on the barrier
			staticBarrier.arriveAndAwaitAdvance();
		}
		catch(IllegalStateException ise){
			///\fixme{Register in advance, dont force the exception}
			staticBarrier.register();
			staticBarrier.arriveAndAwaitAdvance();
		}
	}
	
	@Override
	public synchronized ArrayList<FailureCtrl> purge(FailureCtrl nodeFctrl) {
		throw new RuntimeException("TODO");
	}
	
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}
}
