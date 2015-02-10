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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class Source implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	
	public void setUp() {
	}

	public void processData(DataTuple dt) {
		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		
		long tupleId = 0;
		
		boolean sendIndefinitely = Boolean.parseBoolean(GLOBALS.valueFor("sendIndefinitely"));
		long numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
		long frameRate = Long.parseLong(GLOBALS.valueFor("frameRate"));
		long interFrameDelay = 1000 / frameRate;
		
		final String value = generateFrame(tupleSizeChars);
		
		System.out.println("Source sending started at t="+System.currentTimeMillis());
		while(sendIndefinitely || tupleId < numTuples){
			
			DataTuple output = data.newTuple(tupleId, value);
			System.out.println("Source sending tuple id="+tupleId+",t="+output.getPayload().timestamp);
			api.send_highestWeight(output);
			
			
			/*
			try {
				Thread.sleep(interFrameDelay);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			tupleId++;
		}
	}
	
	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private String generateFrame(int tupleSizeChars)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tupleSizeChars; i++)
		{
			builder.append('x');
		}
		return builder.toString();
	}
}
