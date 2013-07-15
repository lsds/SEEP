/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.processingunit;

import java.util.concurrent.Semaphore;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.operator.Operator;
import uk.co.imperial.lsds.seep.operator.State;
import uk.co.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.co.imperial.lsds.seep.runtimeengine.InputQueue;

import com.esotericsoftware.kryo.Kryo;

public class StatefulProcessingWorker implements Runnable{

//	private DataStructureAdapter dsa;
	private InputQueue iq;
	private Operator runningOp;
	private State state;
	
	private Semaphore executorMutex;
	
//	private Kryo k;
	
	public StatefulProcessingWorker(DataStructureAdapter dsa, Operator op, State s, Semaphore executorMutex) {
		if(dsa.getDSO() instanceof InputQueue){
			this.iq = (InputQueue) dsa.getDSO();
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
