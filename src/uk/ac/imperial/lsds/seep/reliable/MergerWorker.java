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
package uk.ac.imperial.lsds.seep.reliable;

import java.util.concurrent.ArrayBlockingQueue;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;

public class MergerWorker implements Runnable{

	private StatefulProcessingUnit pu;
	private ArrayBlockingQueue<StateChunk> jobQueue;
	
	public MergerWorker(IProcessingUnit pu, ArrayBlockingQueue<StateChunk> jobQueue){
		this.pu = (StatefulProcessingUnit)pu;
		this.jobQueue = jobQueue;
	}
	
	@Override
	public void run() {
		boolean goOn = true;
		while(goOn){
			StateChunk sc = null;
			try {
				sc = jobQueue.take();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(sc.getTotalChunks() == -1){
				goOn = false;
			}
			else{
				pu.mergeChunkToState(sc);
			}
		}
	}
}
