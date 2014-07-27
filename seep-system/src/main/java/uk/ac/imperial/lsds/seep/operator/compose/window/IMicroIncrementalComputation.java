package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

public interface IMicroIncrementalComputation {
	
	public void enteredWindow(MultiOpTuple tuple);

	public void exitedWindow(MultiOpTuple tuple);

	public void evaluateWindow(IWindowAPI api);

}
