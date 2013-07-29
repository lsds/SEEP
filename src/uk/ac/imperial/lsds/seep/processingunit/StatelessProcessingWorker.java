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

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.InputQueue;

public class StatelessProcessingWorker implements Runnable{

	private InputQueue iq;
	private Operator runningOp;
	
	public StatelessProcessingWorker(DataStructureAdapter dsa, Operator runningOp) {
		if(dsa.getDSO() instanceof InputQueue){
			this.iq = (InputQueue) dsa.getDSO();
		}
		else{
			NodeManager.nLogger.severe("-> Operation not permitted at this moment.. stateful multi-core on dist barrier");
		}
		this.runningOp = runningOp;
	}

	@Override
	public void run() {
		while(true){
			DataTuple dt = iq.pull();
			runningOp.processData(dt);
		}
	}

}
