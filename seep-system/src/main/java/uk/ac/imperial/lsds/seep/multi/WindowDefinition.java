package uk.ac.imperial.lsds.seep.multi;

public class WindowDefinition {

	public enum WindowType  {
		ROW_BASED , RANGE_BASED 
	}
	
	private long size = -1;
	private long slide = -1;
	private WindowType windowType;
	
	public WindowDefinition(WindowType windowType, long size, long slide) {
		this.windowType = windowType;
		this.size = size;
		this.slide = slide;
	}
	
	public long getSize() {
		return this.size;
	}

	public long getSlide() {
		return this.slide;
	}

	public WindowType getWindowType() {
		return this.windowType;
	}

}
