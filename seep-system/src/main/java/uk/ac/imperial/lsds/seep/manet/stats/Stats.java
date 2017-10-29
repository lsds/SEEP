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

	private final long MIN_INTERVAL= 5 * 1000;
	//private final long UTIL_MIN_INTERVAL= 5 * 1000;
	private final long UTIL_MIN_INTERVAL= 1 * 1000;
	private final int operatorId;
	private final Integer upstreamId;
	private long tIntervalStart = System.currentTimeMillis();
	private long tFirst = -1;
	private long byteCount = 0;
	private long cumByteCount = 0;
	private long intervalWorkDone = 0;
	private long cumWorkDone = 0;

	public Stats(int operatorId)
	{
		this.operatorId = operatorId;
		this.upstreamId = null;
	}

	public Stats(int operatorId, int upstreamId)
	{
		this.operatorId = operatorId;
		this.upstreamId = upstreamId;
	}

	//TODO: Initial tStart?
	public IntervalTput reset(long t)
	{
		long interval = t - tIntervalStart;

		double intervalTput = computeTput(byteCount, interval);
		byteCount = 0;
		tIntervalStart = t;
		
		long cumInterval = t - tFirst;
		double cumIntervalTput = cumInterval > 0 ? computeTput(cumByteCount, cumInterval) : 0;
		
		return new IntervalTput(t, operatorId, upstreamId, interval, intervalTput, cumIntervalTput);
	}

	public IntervalTput add(long t, long bytes)
	{
		if (tFirst < 0) { tFirst = System.currentTimeMillis(); }
		
		byteCount+=bytes;
		cumByteCount +=bytes;
		
		if (t - tIntervalStart > MIN_INTERVAL)
		{
			IntervalTput tput = reset(t);
			if (upstreamId == null) { logger.info(tput.toString()); }
			return tput;
		}
		return null;
	}

	public IntervalUtil addWorkDone(long t, long workDone)
	{
		if (tFirst < 0) { tFirst = System.currentTimeMillis(); }
		
		intervalWorkDone += workDone;
		cumWorkDone += workDone;
		
		if (t - tIntervalStart > UTIL_MIN_INTERVAL)
		{
			IntervalUtil util = resetUtil(t);
			logger.info(util.toString());
			return util;
		}
		return null;
	}

	public IntervalUtil resetUtil(long t)
	{
		long interval = t - tIntervalStart;

		double intervalUtil = computeUtil(intervalWorkDone, interval);
		intervalWorkDone = 0;
		tIntervalStart = t;
		
		long cumInterval = t - tFirst;
		double cumIntervalUtil = cumInterval > 0 ? computeUtil(cumWorkDone, cumInterval) : 0;
		
		return new IntervalUtil(t, operatorId, interval, intervalUtil, cumIntervalUtil);
	}
	

	private double computeTput(long bytes, long interval)
	{
		if (interval < 0) { throw new RuntimeException("Logic error."); }
		if (interval == 0) { return 0; }
		return ((8 * bytes * 1000) / interval)/1024;
	}

	private double computeUtil(long work, long interval)
	{
		logger.info("Computing util with work="+work+",interval="+interval);
		if (interval < 0) { throw new RuntimeException("Logic error."); }
		if (interval == 0) { return 0; }
		return ((double)work) / ((double)interval);
	}

	public static class IntervalTput
	{
		public final long t;
		public final int operatorId;
		public final Integer upstreamId;
		public final long interval;
		public final double intervalTput;
		public final double cumIntervalTput;

		public IntervalTput(long t, int operatorId, Integer upstreamId, long interval, double intervalTput, double cumIntervalTput)
		{
			this.t = t;
			this.operatorId = operatorId;
			this.upstreamId = upstreamId;
			this.interval= interval;
			this.intervalTput = intervalTput;
			this.cumIntervalTput = cumIntervalTput;	
		}

		@Override
		public String toString()
		{
			if (upstreamId == null)	
			{
				return "t="+t+",id="+operatorId+",interval="+interval+",tput="+intervalTput+",cumTput="+cumIntervalTput;
			}
			else
			{
				return "t="+t+",opid="+operatorId+",upid="+upstreamId+",interval="+interval+",tput="+intervalTput+",cumTput="+cumIntervalTput;
			}
		}
	}

	public static class IntervalUtil
	{
		public final long t;
		public final int operatorId;
		public final long interval;
		public final double intervalUtil;
		public final double cumUtil;	

		public IntervalUtil(long t, int operatorId, long interval, double intervalUtil, double cumUtil)
		{
			this.t = t;
			this.operatorId = operatorId;
			this.interval= interval;
			this.intervalUtil = intervalUtil;
			this.cumUtil = cumUtil;	
		}

		@Override
		public String toString()
		{
			return "t="+t+",id="+operatorId+",interval="+interval+",util="+intervalUtil+",cumUtil="+cumUtil;
		}
	}
}
