package uk.ac.imperial.lsds.seep.operator.compose.window;

public interface IPeriodicWindowBatch extends IWindowBatch {

	/*
	 * Getters and setters for values that define the window batch 
	 */
	public int getStart();
	public int getEnd();
	public void setStart(int start);
	public void setEnd(int end);

	/*
	 * Getter and setter method for the definition of the window
	 */
	public IWindowDefinition getWindowDefinition();
	public void setWindowDefinition(IWindowDefinition windowDefinition);
	
}
