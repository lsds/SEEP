package uk.ac.imperial.lsds.seep.operator.compose.window;

public interface IPeriodicWindowBatch extends IWindowBatch {

	/*
	 * Get the indices of the start and the end in the respective
	 * buffer
	 */
	public int getStartIndex();
	public int getEndIndex();

	public long getStartTimestamp();
	public long getEndTimestamp();

	/*
	 * Getter and setter method for the definition of the window
	 */
	public IWindowDefinition getWindowDefinition();
	public void setWindowDefinition(IWindowDefinition windowDefinition);
	
}
