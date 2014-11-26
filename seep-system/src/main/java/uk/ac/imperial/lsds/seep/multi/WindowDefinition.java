package uk.ac.imperial.lsds.seep.multi;

public class WindowDefinition {
	
	public enum WindowType { ROW_BASED , RANGE_BASED }
	
	private WindowType type;
	private long size;
	private long slide;
	
	private long paneSize;
	
	public WindowDefinition (WindowType type, long size, long slide) {
		this.type  =  type;
		this.size  =  size;
		this.slide = slide;
		this.paneSize = gcd (this.size, this.slide);
	}
	
	public long getSize () {
		return this.size;
	}
	
	public long getSlide () {
		return this.slide;
	}
	
	public WindowType getWindowType () {
		return this.type;
	}
	
	public long getPaneSize () {
		return this.paneSize;
	}
	
	public long numberOfPanes () {
		return (this.size / this.paneSize);
	}
	
	public long panesPerSlide () {
		return (this.slide / this.paneSize);
	}
	
	public boolean isRowBased () {
		return (this.type == WindowType.ROW_BASED);
	}
	
	public boolean isRangeBased () {
		return (this.type == WindowType.RANGE_BASED);
	}
	
	public boolean isTumbling () {
		return (this.size == this.slide);
	}
	
	private static long gcd (long a, long b) {
		if (b == 0) 
			return a;
		return 
			gcd (b, a % b);
	}
}
