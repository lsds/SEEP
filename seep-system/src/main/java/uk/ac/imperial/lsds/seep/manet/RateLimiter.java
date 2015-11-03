package uk.ac.imperial.lsds.seep.manet;

public interface RateLimiter {

	public void setLimit(long tuplesPerSecond);
	public void limit();
}
