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
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.acita15.heatmap.*;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

public class HeatMapJoin implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Join.class);
	private int processed = 0;
	private Stats stats;
	private final long processingDelay = Long.parseLong(GLOBALS.valueFor("defaultProcessingDelay"));
	
	public void processData(DataTuple data) {
		logger.error("Should never be called for a join op!");
		System.exit(1);
	}

	
	public void processData(List<DataTuple> arg0) {
		if (arg0.size() != 2) { throw new RuntimeException("Logic error - should be 2 tuples, 1 tuple per input for a binary join."); }
		
		logger.debug("Processing tuples: "+arg0);
		
		//Generate the output batch from the earlier of the two batches (in terms of real world timestamp).
		DataTuple data = null;
		for (DataTuple dt : arg0)
		{
			if (dt != null) 
			{ 
				recordTuple(dt);
				if (data == null || 
						data.getPayload().instrumentation_ts < dt.getPayload().instrumentation_ts)
				{
					data = dt;
				}
			}
			else { logger.info("Null tuple"); }
		}
	
		long tupleId = data.getLong("tupleId");
		
		
		String value = mergeHeatMaps(getHeatMaps(arg0)).toString();
		int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
		String padding = generatePadding(tupleSizeChars - value.length());
		
		//String value = data.getString("value") + "," + api.getOperatorId();
		//String value = data.getString("value");
		
		
		DataTuple outputTuple = data.setValues(tupleId, value, padding);
		processed++;
		if (processed == 1 || processed % 1000 == 0)
		{
			logger.info("Join operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Join operator "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			recordTuple(outputTuple);
		} 

		doProcessing();

		stats.add(System.currentTimeMillis(), outputTuple.getPayload().toString().length());
		api.send_highestWeight(outputTuple);
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
		System.out.println("Setting up HEATMAP_JOIN operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
	}

	public List<HeatMap> getHeatMaps(List<DataTuple> tuples)
	{
		List<HeatMap> result = new LinkedList<>();
		for (DataTuple tuple : tuples)
		{
			if (tuple != null)
			{
				result.add(new HeatMap(tuple.getString("value")));
			}
		}
		return result;
	}
	
	public HeatMap mergeHeatMaps(List<HeatMap> heatMaps)
	{
		HeatMap result = null;
		for (HeatMap heatMap : heatMaps)
		{
			if (result == null) { result = heatMap; }
			else { result.add(heatMap); }
		}
		
		return result;
	}
	
	private String generatePadding(int tupleSizeChars)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tupleSizeChars; i++)
		{
			builder.append('x');
		}
		return builder.toString();
	}

	private void doProcessing()
	{
		if (processingDelay > 0)
		{	
			try { Thread.sleep(processingDelay); }
			catch (InterruptedException e) { }
		}
	}
}
