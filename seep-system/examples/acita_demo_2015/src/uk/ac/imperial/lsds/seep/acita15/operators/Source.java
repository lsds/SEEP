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
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class Source implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Source.class);
	private static boolean scheduledPauses = false;
	
	public void setUp() {
		System.out.println("Setting up SOURCE operator with id="+api.getOperatorId());
		scheduledPauses = Boolean.parseBoolean(GLOBALS.valueFor("scheduledPauses"));
	}

	public void processData(DataTuple dt) {
		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		logger.info("Source using mapper="+mapper);			
		long tupleId = 0;
		
		boolean sendIndefinitely = Boolean.parseBoolean(GLOBALS.valueFor("sendIndefinitely"));
		long numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		long warmUpTuples = Long.parseLong(GLOBALS.valueFor("warmUpTuples"));
		int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
		boolean rateLimitSrc = Boolean.parseBoolean(GLOBALS.valueFor("rateLimitSrc"));
		long frameRate = Long.parseLong(GLOBALS.valueFor("frameRate"));
		long interFrameDelay = 1000 / frameRate;
		logger.info("Source inter-frame delay="+interFrameDelay);
		
		initialPause();

		final String value = generateFrame(tupleSizeChars);
		final long[] latencyBreakdown = new long[0];
		final long tStart = System.currentTimeMillis();
		while(sendIndefinitely || tupleId < numTuples + warmUpTuples){
			if (tupleId == warmUpTuples)
			{ 
				long tWarmedUp = System.currentTimeMillis();
				logger.info("Source sending started at t="+tWarmedUp);
				logger.info("Source sending started at t="+tWarmedUp);
				logger.info("Source sending started at t="+tWarmedUp);
			}
		
			schedulePause(tupleId);	

			DataTuple output = data.newTuple(tupleId, value, latencyBreakdown);
			output.getPayload().timestamp = tupleId;
			if (tupleId % 1000 == 0)
			{
				logger.info("Source sending tuple id="+tupleId+",t="+output.getPayload().instrumentation_ts);
			}
			else
			{
				logger.debug("Source sending tuple id="+tupleId+",t="+output.getPayload().instrumentation_ts);
			}
			api.send_highestWeight(output);
			
			tupleId++;
			
			long tNext = tStart + (tupleId * interFrameDelay);
			long tNow = System.currentTimeMillis();
			if (tNext > tNow && rateLimitSrc)
			{
				logger.debug("Source wait to send next frame="+(tNext-tNow));
				try {
					Thread.sleep(tNext - tNow);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}

		while(!sendIndefinitely)
		{
			try {
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}				
			logger.info("Source waiting for sink to terminate.");
		}

		System.exit(0);
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

	private void schedulePause(long ts)
	{
		if (scheduledPauses)
		{
			//long pauseOnTuple = 5000;
			long pauseOnTuple = 3000;
			if (ts == pauseOnTuple)
			{
				File startFailures = new File("../start_failures.txt");
				if (startFailures.exists())
				{
					long tStart = readStartTime(startFailures);
					long initialPause = Long.parseLong(GLOBALS.valueFor("initialPause"));
					long now = System.currentTimeMillis();
					long schedulePause = 100 * 1000; 
					if (now < tStart + initialPause + schedulePause)
					{
						schedulePause = tStart + initialPause + schedulePause - now;
					}
					else
					{
						logger.error("Took to long to reach schedule point, missed time by "+ (now - (tStart + initialPause + schedulePause)) + " ms.");
						System.exit(1);
					}

					try {
						Thread.sleep(schedulePause);
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}				
				}
			}
		}
	}

	private void initialPause()
	{
		long pause = Long.parseLong(GLOBALS.valueFor("initialPause"));
		long now = System.currentTimeMillis();
		File startFailures = new File("../start_failures.txt");
		if (startFailures.exists())
		{
			long tStart = readStartTime(startFailures);
			logger.info("Read start time: "+tStart);
			if (now < tStart + pause)
			{
				pause = tStart + pause - now;	
				logger.info("Waiting for "+pause+" ms.");
			}
			else
			{
				logger.error("Startup took to long, missed start time by "+ (now - (tStart + pause)) + " ms.");
				System.exit(1);
			}
		}

		try {
			Thread.sleep(pause);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}				
	}

	private long readStartTime(File f)
	{
		String line = null;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(f));
			line = br.readLine();
		}
		catch (IOException e)
		{
			logger.error("Error reading start time:", e);
			System.exit(1);
		}
		return (long)(1000 * Double.parseDouble(line.trim()));
	}

}
