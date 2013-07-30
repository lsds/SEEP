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
package uk.ac.imperial.lsds.seep.processingunit;

import java.util.concurrent.Semaphore;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.InputQueue;

public class StatefulProcessingWorker implements Runnable{

	private InputQueue iq;
	private Operator runningOp;
	private State state;
	private Semaphore executorMutex;
	
	///\todo{for now, multicore is only supported in those operators with only one inputDataIngestionMode}
	public StatefulProcessingWorker(DataStructureAdapter dsa, Operator op, State s, Semaphore executorMutex) {
		if(dsa.getUniqueDso() != null){
			this.iq = (InputQueue) dsa.getUniqueDso();
		}
		else{
			NodeManager.nLogger.severe("-> Operation not permitted at this moment.. stateful multi-core on dist barrier");
		}
		this.runningOp = op;
		this.state = s;
		this.executorMutex = executorMutex;
	}

	@Override
	public void run() {
		while(true){
			try {
				executorMutex.acquire();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			DataTuple dt = iq.pull();
			runningOp.processData(dt);
			
			executorMutex.release();
		}
	}
}
