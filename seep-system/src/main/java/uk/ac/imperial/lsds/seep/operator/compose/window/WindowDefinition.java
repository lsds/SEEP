package uk.ac.imperial.lsds.seep.operator.compose.window;

public class WindowDefinition implements IWindowDefinition {

	public enum WindowType  {
		ROW_BASED, RANGE_BASED
	}
	
	private long size = -1;
	private long slide = -1;
	private WindowType windowType;
	
	public WindowDefinition(WindowType windowType, long size, long slide) {
		this.windowType = windowType;
		this.size = size;
		this.slide = slide;
	}
	
	@Override
	public long getSize() {
		return this.size;
	}

	@Override
	public long getSlide() {
		return this.slide;
	}

	@Override
	public WindowType getWindowType() {
		return this.windowType;
	}

}
