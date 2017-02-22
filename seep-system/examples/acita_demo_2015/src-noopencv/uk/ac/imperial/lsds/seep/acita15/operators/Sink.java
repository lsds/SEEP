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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

public class Sink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Sink.class);
	private long numTuples;
	private long warmUpTuples;
	private long tupleSize;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	private boolean enableLatencyBreakdown = false;
	private Stats stats = null;
	
	public void setUp() {
		logger.info("Setting up SINK operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		warmUpTuples = Long.parseLong(GLOBALS.valueFor("warmUpTuples"));
		tupleSize = Long.parseLong(GLOBALS.valueFor("tupleSizeChars"));
		enableLatencyBreakdown = Boolean.parseBoolean(GLOBALS.valueFor("enableLatencyBreakdown"));
		logger.info("SINK expecting "+numTuples+" tuples.");
	}
	
	public void processData(DataTuple dt) {
		if (dt.getPayload().timestamp != dt.getLong("tupleId"))
		{
			throw new RuntimeException("Logic error: ts " + dt.getPayload().timestamp+ "!= tupleId "+dt.getLong("tupleId"));
		}
		
		if (dt.getLong("tupleId") < warmUpTuples) 
		{ 
			logger.debug("Ignoring warm up tuple "+dt.getLong("tupleId")); 
			api.ack(dt);
			return;
		}

		if (tuplesReceived == 0)
		{
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
		}
		
		tuplesReceived++;
		//totalBytes += dt.getByteArray("value").length;
		totalBytes += dt.getString("value").length();
		recordTuple(dt, dt.getString("value").length());
		long tupleId = dt.getLong("tupleId");
		if (tupleId != warmUpTuples + tuplesReceived -1)
		{
			logger.info("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if (tuplesReceived >= numTuples)
		{
			logger.info("SNK: FINISHED with total tuples="+tuplesReceived
					+",total bytes="+totalBytes
					+",t="+System.currentTimeMillis()
					+",tuple size bytes="+tupleSize);
			System.exit(0);
		}
		stats.add(System.currentTimeMillis(), dt.getPayload().toString().length());
		api.ack(dt);
	}
	
	private void recordTuple(DataTuple dt, int bytes)
	{
		long rxts = System.currentTimeMillis();
		logger.info("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts)
				+",bytes="+ bytes
				+",latencyBreakdown="+getLatencyBreakdown(dt, rxts-dt.getPayload().instrumentation_ts));
	}
	
	public void processData(List<DataTuple> arg0) {
		throw new RuntimeException("TODO");
	}
	
	private String getLatencyBreakdown(DataTuple dt, long latency)
	{
		if (!enableLatencyBreakdown) { return "0;0"; }
		if (dt.getMap().containsKey("latencyBreakdown"))
		{
			long[] latencies = dt.getLongArray("latencyBreakdown");
			String result = "";

			long opLatency = 0;
			long socketLatency = 0;
			logger.info("latency breakdown length="+latencies.length);
			for (int i = 0; i < latencies.length; i=i+3)
			{
				opLatency += latencies[i];
				socketLatency += Math.min(latencies[i+1], latencies[i+2]);
				//result += ""+ latencies[i] + ";"+ latencies[i+1] + ";" + latencies[i+2];
				//if (i < latencies.length - 1) { result += ":"; }
			}	
			//result += ""+opLatency+";"+socketLatency+";"+(100 * opLatency / latency);	
			result += ""+opLatency+";"+socketLatency;	
			return result;
		}
		else
		{
			return "null";
		}
	}
}
