package uk.ac.imperial.lsds.seep.manet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedRateLimiter implements RateLimiter {
	private static final Logger logger = LoggerFactory.getLogger(FixedRateLimiter.class);
	private long limit;
	private long minDelayMillis;
	private long tLast;
	
	public FixedRateLimiter()
	{
		limit = Long.MAX_VALUE;
		minDelayMillis = 0;
		tLast = System.currentTimeMillis();
	}
	
	
	public void setLimit(long tuplesPerSecond)
	{
		limit = tuplesPerSecond;
		minDelayMillis = 1000/limit;
		logger.info("Set limit to "+tuplesPerSecond+" tuples/s, minDelay="+minDelayMillis);
	}
	
	public void limit() {
		
		if (minDelayMillis > 0)
		{
			long tNow = System.currentTimeMillis();
			if (tNow - tLast < minDelayMillis)
			{
				try {
					Thread.sleep(minDelayMillis - (tNow - tLast));
				} catch (InterruptedException e) {
					throw new RuntimeException("Logic error.");
				}
				logger.debug("Fixed rate limiter waited for t="+(minDelayMillis - (tNow - tLast)));
			}
			else
			{
				logger.trace("Fixed rate limiter no delay, tnow-tlast="+(tNow - tLast));
			}
			tLast = System.currentTimeMillis();
		}
	}
}
