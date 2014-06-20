package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition.WindowType;

public interface IWindowDefinition {
	
	/*
	 * Getters for values that define the window 
	 */
	public long getSize();
	public long getSlide();

	/*
	 * Check method for the type of window
	 */
	public WindowType getWindowType();
}
