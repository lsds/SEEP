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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;
import uk.ac.imperial.lsds.seep.acita15.heatmap.*;

public class HeatMapSink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(HeatMapSink.class);
	private long numTuples;
	private long warmUpTuples;
	private long tupleSize;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	private Stats stats = null;
	private HeatMap result = null;
	private int numSources;
	private double averageCoverage = 0.0;
	private final boolean enableSinkDisplay = Boolean.parseBoolean(GLOBALS.valueFor("enableSinkDisplay"));
	private final int displayPort = 20150;
	private final String displayAddr = "172.16.0.254";
	private Socket displaySocket = null;
	private ObjectOutputStream output = null;
	
	public void setUp() {
		logger.info("Setting up SINK operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		warmUpTuples = Long.parseLong(GLOBALS.valueFor("warmUpTuples"));
		tupleSize = Long.parseLong(GLOBALS.valueFor("tupleSizeChars"));
		numSources = Integer.parseInt(GLOBALS.valueFor("sources"));
		logger.info("SINK expecting "+numTuples+" tuples.");
		
		connectToDisplay();
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
		String value = dt.getString("value");
		String padding = dt.getString("padding");
		totalBytes += value.length();
		totalBytes += padding.length();
		recordTuple(dt, value.length() + padding.length());
		
		HeatMap update = new HeatMap(value);
		if (result == null) { result = update; }
		else 
		{
			int sourceIdCount = update.getSourceIds().size();
			double updateCoverage = (100 * sourceIdCount) / numSources;
			averageCoverage += (updateCoverage - averageCoverage) / tuplesReceived;
			logger.debug("Coverage, update="+updateCoverage+",cumavg="+averageCoverage);
			
			result.add(update);
			//TODO: Might want to have some kind of window here.
			displayHeatMap(result.toString());
		}
		logger.debug("Current heatmap="+result.toString());
		
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
					+",tuple size bytes="+tupleSize
					+",average coverage="+averageCoverage);
			
			logger.info("Recording final heatmap: "+result.toString());
			System.exit(0);
		}
		stats.add(System.currentTimeMillis(), dt.getPayload().toString().length());
		api.ack(dt);
	}
	
	private void recordTuple(DataTuple dt, int bytes)
	{
		long rxts = System.currentTimeMillis();
		/*
		logger.info("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts)
				+",bytes="+ bytes);
		*/
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
	
	private void connectToDisplay()
	{
		if (!enableSinkDisplay) { return; }
		try
		{
			displaySocket = new Socket(displayAddr, displayPort);
			output = new ObjectOutputStream(displaySocket.getOutputStream());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void displayHeatMap(String heatMapStr)
	{
		if (enableSinkDisplay)
		{
			try
			{
				logger.info("Sending heatMap to display: "+ heatMapStr);
				output.writeObject(heatMapStr);
				output.flush();
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
    private String posCountsToString(int[][] posCounts)
    { 
      String occupiedTiles = "";

      for (int x = 0; x < posCounts.length; x++)
      { 
        for (int y = 0; y < posCounts[0].length; y++)
        { 
          if (posCounts[x][y] > 0)
          { 
            String tileCount = "" + x + "," + y + "," + posCounts[x][y];

            if (!occupiedTiles.isEmpty()) { occupiedTiles += ";"; }
            occupiedTiles += tileCount;
          }
        }
      }
      return occupiedTiles;
    }

	private String getLatencyBreakdown(DataTuple dt, long latency)
	{ 
		return "0;0"; 
	}

}
