/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.acita15.operators;

import java.util.List;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private long numTuples;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	
	public void setUp() {
		System.out.println("Setting up SINK operator with id="+api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		System.out.println("SINK expecting "+numTuples+" tuples.");
	}
	
	public void processData(DataTuple dt) {
		
		if (tuplesReceived == 0)
		{
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
		}
		
		tuplesReceived++;
		totalBytes += dt.getString("value").length();
		recordTuple(dt);
		long tupleId = dt.getLong("tupleId");
		if (tupleId != tuplesReceived -1)
		{
			System.out.println("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if (tuplesReceived >= numTuples)
		{
			System.out.println("SNK: FINISHED with total tuples="+tuplesReceived+",total bytes="+totalBytes+",t="+System.currentTimeMillis());
			System.exit(0);
		}
	}
	
	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		System.out.println("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",txts="+dt.getPayload().timestamp
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().timestamp));
	}
	
	public void processData(List<DataTuple> arg0) {
	}
}
