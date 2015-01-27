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

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final long MAX_TUPLES = 10;
	
	public void setUp() {

	}

	// time control variables
	int c = 0;
	long init = 0;
	int sec = 0;
	long tuplesReceived = 0;
	
	public void processData(DataTuple dt) {
		System.out.println("Sink received "+dt.toString());
		long tupleId = dt.getLong("tupleId");
		String value = dt.getString("value");
		// TIME CONTROL
		tuplesReceived++;
		
		if (tupleId != tuplesReceived -1)
		{
			System.out.println("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if((System.currentTimeMillis() - init) > 1000){
			System.out.println("SNK: "+sec+" "+c+" ");
			c = 0;
			sec++;
			init = System.currentTimeMillis();
		}
		if (tuplesReceived >= MAX_TUPLES)
		{
			System.out.println("SNK: FINISHED with total tuples="+tuplesReceived);
			System.exit(0);
		}
	}
	
	public void processData(List<DataTuple> arg0) {
	}
}
