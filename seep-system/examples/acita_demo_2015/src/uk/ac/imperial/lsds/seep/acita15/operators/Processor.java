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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

public class Processor implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Processor.class);
	private int processed = 0;
	private Stats stats;
	private Stats utilStats;
	private long processingDelay = Long.parseLong(GLOBALS.valueFor("defaultProcessingDelay"));
	
	public void processData(DataTuple data) {
		long tProcessStart = System.currentTimeMillis();
		long tupleId = data.getLong("tupleId");
		String value = data.getString("value") + "," + api.getOperatorId();
		
		DataTuple outputTuple = data.setValues(tupleId, value, data.getLongArray("latencyBreakdown"));
		processed++;
		if (processed == 1 || processed % 1000 == 0)
		{
			logger.info("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			if (logger.isDebugEnabled())
			{
				recordTuple(outputTuple);
			}
		}
		
		doProcessing();
		
		long tProcessEnd = System.currentTimeMillis();
		stats.add(tProcessEnd, data.getPayload().toString().length());
		utilStats.addWorkDone(tProcessEnd, tProcessEnd - tProcessStart);
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
		for (DataTuple data : arg0)
		{
			long tupleId = data.getLong("tupleId");
			String value = data.getString("value") + "," + api.getOperatorId();
			
			DataTuple outputTuple = data.setValues(tupleId, value);
			processed++;
			if (processed % 1000 == 0)
			{
				logger.info("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			}
			else
			{
				logger.debug("Operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
				recordTuple(outputTuple);
			}
		}
		throw new RuntimeException("TODO"); 
	}

	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		logger.debug("OP: "+api.getOperatorId()+" received tuple with id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void setUp() {
		System.out.println("Setting up PROCESSOR operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		utilStats = new Stats(api.getOperatorId());
		setProcessingDelay();
	}

	private void doProcessing()
	{
		if (processingDelay > 0)
		{	
			try { Thread.sleep(processingDelay); }
			catch (InterruptedException e) { }
		}
	}

	//Tmp hack
	private void setProcessingDelay()
	{
		/*
		if (api.getOperatorId() == 0 || api.getOperatorId() == 10 || api.getOperatorId() == 11)
		{ processingDelay = 80; } 
		else if (api.getOperatorId() == 2 || api.getOperatorId() == 210 || api.getOperatorId() == 211)
		{ processingDelay = 100; } 
		else { processingDelay = 0; }
		*/
		logger.warn("Setting explicit processing delay of "+processingDelay+" for operator "+api.getOperatorId());
	}
		
		
}
