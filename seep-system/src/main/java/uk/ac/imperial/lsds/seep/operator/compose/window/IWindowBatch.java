package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public interface IWindowBatch {

	public long getStartTimestamp();
	public long getEndTimestamp();

	public int[] getWindowStartPointers();
	public int[] getWindowEndPointers();
	
	public SubQueryBuffer getBufferContent();
	public MultiOpTuple[] getArrayContent();

	public MultiOpTuple get(int index);
	
	public void performIncrementalComputation(IMicroIncrementalComputation incrementalComputation, IWindowAPI api);

	public void addWindow(MultiOpTuple[] window);

}
