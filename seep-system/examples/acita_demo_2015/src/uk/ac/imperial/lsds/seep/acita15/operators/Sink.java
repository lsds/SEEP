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
import java.io.Serializable;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private long numTuples;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	private final Stats stats = new Stats();
	
	public void setUp() {
		System.out.println("Setting up SINK operator with id="+api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		System.out.println("SINK expecting "+numTuples+" tuples.");
	}
	
	public void processData(DataTuple dt) {
		if (dt.getPayload().timestamp != dt.getLong("tupleId"))
		{
			throw new RuntimeException("Logic error: ts " + dt.getPayload().timestamp+ "!= tupleId "+dt.getLong("tupleId"));
		}
		if (tuplesReceived == 0)
		{
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
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
		stats.add(System.currentTimeMillis(), dt.getPayload().toString().length());
		api.ack(dt);
	}
	
	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		System.out.println("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void processData(List<DataTuple> arg0) {
	}
	private static class Stats implements Serializable {
		private final long MIN_INTERVAL= 1 * 1000;
		private long tStart = System.currentTimeMillis();
		private long byteCount = 0;

		//TODO: Initial tStart?
		public String reset(long t)
		{
			long interval = t - tStart;
			
			double intervalTput = computeTput(byteCount, interval);
			byteCount = 0;
			tStart = t;
			return "t="+t+",interval="+interval+",tput="+intervalTput;
		}

		public void add(long t, long bytes)
		{
			byteCount+=bytes;
			if (t - tStart > MIN_INTERVAL)
			{
				System.out.println(reset(t));
			}
		}

		private double computeTput(long bytes, long interval)
		{
			if (interval < 0) { throw new RuntimeException("Logic error."); }
			if (interval == 0) { return 0; }
			return ((8 * bytes * 1000) / interval)/1024;
		}

	}
}
