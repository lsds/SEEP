package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

public class KeyBounds {

	private int minBound;
	private int maxBound;
	
	public KeyBounds(){}
	
	public KeyBounds(int minBound, int maxBound) {
		this.minBound = minBound;
		this.maxBound = maxBound;
	}

	public int getMinBound() {
		return minBound;
	}

	public void setMinBound(int minBound) {
		this.minBound = minBound;
	}

	public int getMaxBound() {
		return maxBound;
	}

	public void setMaxBound(int maxBound) {
		this.maxBound = maxBound;
	}
	
}
