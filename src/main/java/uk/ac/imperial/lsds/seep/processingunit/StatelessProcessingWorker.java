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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.InputQueue;

public class StatelessProcessingWorker implements Runnable{
	
	final private Logger LOG = LoggerFactory.getLogger(StatelessProcessingWorker.class);

	private InputQueue iq;
	private Operator runningOp;
	
	///\todo{for now, multicore is only supported in those operators with only one inputDataIngestionMode}
	public StatelessProcessingWorker(DataStructureAdapter dsa, Operator runningOp) {
		if(dsa.getUniqueDso() != null){
			this.iq = (InputQueue) dsa.getUniqueDso();
		}
		else{
			LOG.error("-> Operation not permitted at this moment.. stateful multi-core on dist barrier");
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
