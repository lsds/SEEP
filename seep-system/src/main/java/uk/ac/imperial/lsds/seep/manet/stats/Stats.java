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
package uk.ac.imperial.lsds.seep.manet.stats;

import java.util.List;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Stats implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Stats.class);

	private final long MIN_INTERVAL= 1 * 1000;
	private final int operatorId;
	private long tIntervalStart = System.currentTimeMillis();
	private long tFirst = -1;
	private long byteCount = 0;
	private long cumByteCount = 0;

	public Stats(int operatorId)
	{
		this.operatorId = operatorId;
	}

	//TODO: Initial tStart?
	public String reset(long t)
	{
		long interval = t - tIntervalStart;

		double intervalTput = computeTput(byteCount, interval);
		byteCount = 0;
		tIntervalStart = t;
		
		long cumInterval = t - tFirst;
		double cumIntervalTput = cumInterval > 0 ? computeTput(cumByteCount, cumInterval) : 0;
		
		return "t="+t+",id="+operatorId+",interval="+interval+",tput="+intervalTput+",cumTput="+cumIntervalTput;
	}

	public void add(long t, long bytes)
	{
		if (tFirst < 0) { tFirst = System.currentTimeMillis(); }
		
		byteCount+=bytes;
		cumByteCount +=bytes;
		
		if (t - tIntervalStart > MIN_INTERVAL)
		{
			logger.info(reset(t));
		}
	}

	private double computeTput(long bytes, long interval)
	{
		if (interval < 0) { throw new RuntimeException("Logic error."); }
		if (interval == 0) { return 0; }
		return ((8 * bytes * 1000) / interval)/1024;
	}
}
