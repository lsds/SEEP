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
import uk.ac.imperial.lsds.seep.acita15.stats.Stats;

import uk.ac.imperial.lsds.seep.acita15.heatmap.*;

public class HeatMapSink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(HeatMapSink.class);
	private long numTuples;
	private long tupleSize;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	private Stats stats = null;
	private HeatMap result = null;
	
	public void setUp() {
		logger.info("Setting up SINK operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		tupleSize = Long.parseLong(GLOBALS.valueFor("tupleSizeChars"));
		logger.info("SINK expecting "+numTuples+" tuples.");
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
		//totalBytes += dt.getByteArray("value").length;
		String value = dt.getString("value");
		String padding = dt.getString("padding");
		totalBytes += value.length();
		totalBytes += padding.length();
		recordTuple(dt);
		
		HeatMap update = new HeatMap(value);
		if (result == null) { result = update; }
		else { result.add(update); }
		logger.info("Current heatmap="+result.toString());
		
		long tupleId = dt.getLong("tupleId");
		if (tupleId != tuplesReceived -1)
		{
			logger.info("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if (tuplesReceived >= numTuples)
		{
			logger.info("SNK: FINISHED with total tuples="+tuplesReceived
					+",total bytes="+totalBytes
					+",t="+System.currentTimeMillis()
					+",tuple size bytes="+tupleSize);
			
			logger.info("Recording final heatmap: "+result.toString());
			System.exit(0);
		}
		stats.add(System.currentTimeMillis(), dt.getPayload().toString().length());
		api.ack(dt);
	}
	
	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		logger.info("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void processData(List<DataTuple> arg0) {
		throw new RuntimeException("TODO");
	}
}
