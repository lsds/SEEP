package uk.ac.imperial.lsds.seep.manet;

public class FixedRateLimiter implements RateLimiter {

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
	}
	
	public void limit() {
		
		if (minDelayMillis > 0)
		{
			long tNow = System.currentTimeMillis();
			if (tNow - tLast < minDelayMillis)
			{
				try {
					Thread.sleep(tNow - tLast);
				} catch (InterruptedException e) {
					throw new RuntimeException("Logic error.");
				}
				tLast = System.currentTimeMillis();
			}
		}
	}
}
