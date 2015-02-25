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

public class Processor implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	
	public void processData(DataTuple data) {
		long tupleId = data.getLong("tupleId");
		String value = data.getString("value") + "," + api.getOperatorId();
		
		DataTuple outputTuple = data.setValues(tupleId, value);
		System.out.println("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
		for (DataTuple data : arg0)
		{
			long tupleId = data.getLong("tupleId");
			String value = data.getString("value") + "," + api.getOperatorId();
			
			DataTuple outputTuple = data.setValues(tupleId, value);
			System.out.println("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		throw new RuntimeException("TODO"); 
	}

	
	public void setUp() {
		System.out.println("Setting up PROCESSOR operator with id="+api.getOperatorId());
	}

}
