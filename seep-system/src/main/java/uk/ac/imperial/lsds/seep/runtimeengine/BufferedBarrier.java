

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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import uk.ac.imperial.lsds.seep.GLOBALS;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class BufferedBarrier implements DataStructureI{

	private List<ArrayBlockingQueue<DataTuple>> buffers = new ArrayList<ArrayBlockingQueue<DataTuple>>();
	private Map<Long, Integer> thread_mapper = new HashMap<Long, Integer>();
        private Map<Integer, Integer> upstreamOpId_mapper = new HashMap<Integer, Integer>();
        
        private int repetitionsANN = 0 ;
        private long cummulatedBarrierTime = 0 ; 
        private long barrierTimeEachPhase;
        private DecimalFormat df = new DecimalFormat("#.##");
	
	public BufferedBarrier(ArrayList<Integer> upstreamOpIdList){
            register(upstreamOpIdList);
        }
        
        @Override
	public DataTuple pull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
                
		ArrayList<DataTuple> toReturn = new ArrayList<DataTuple>();
                
                repetitionsANN++;
		boolean isFirstBuffer = true ;
                
                for(ArrayBlockingQueue<DataTuple> buffer : buffers){
                   
                    if (isFirstBuffer) {
                        isFirstBuffer = false;
                        barrierTimeEachPhase = System.nanoTime();
                    }

                    try {
                        toReturn.add(buffer.take());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
		}
                
                cummulatedBarrierTime += System.nanoTime() - barrierTimeEachPhase;
                
                if (repetitionsANN % 100 == 0) {
                    System.out.println("Repetitions = " + repetitionsANN + ", Accum barrier time: " + 
                            df.format(((double) (cummulatedBarrierTime / 1000000000.0))) + " s");
                }
                if (repetitionsANN == 10000) {
                    //System.out.println("Repetitions = " + repetitionsANN + ", Accum barrier time: " + 
                            //df.format(((double) (cummulatedBarrierTime / 1000000000.0))) + " s");
                    repetitionsANN = 0;
                    cummulatedBarrierTime = 0;
                }

                return toReturn;
	} 

	@Override
	public void push(DataTuple dt, int upstreamOpId) {
//		long threadId = Thread.currentThread().getId();
//		int idx = -1;
//		// If already exists
//		if(thread_mapper.containsKey(threadId)){
//			idx = thread_mapper.get(threadId);
//		}
//		// Otherwise we register the thread
//		else{
//			idx = register();
//		}
//		try {
//			buffers.get(idx).put(dt);
//		}
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
                int idx = upstreamOpId_mapper.get(upstreamOpId);
		try {
			buffers.get(idx).put(dt);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
        //Original code
	public int _register(){
		long id = Thread.currentThread().getId();
		ArrayBlockingQueue<DataTuple> buffer = new ArrayBlockingQueue<DataTuple>(10);
		buffers.add(buffer);
		int idx = buffers.size()-1;
		thread_mapper.put(id, idx);
		return idx;
	}
        
        public void register(ArrayList<Integer> upstreamOpIdList){
                //Use info about upstream opids to map these buffers
		for(int opID : upstreamOpIdList){
                    ArrayBlockingQueue<DataTuple> buffer = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("bufferLengthInBarrier")));
                    buffers.add(buffer);
                    int idx = buffers.size()-1;
                    upstreamOpId_mapper.put(opID, idx);
                }
                System.out.println("Register with " + buffers.size() + " buffers.");
	}

}

