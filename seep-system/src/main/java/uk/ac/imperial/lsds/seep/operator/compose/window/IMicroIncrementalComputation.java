package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IMicroIncrementalComputation {
	
	public void enteredWindow(DataTuple tuple);

	public void exitedWindow(DataTuple tuple);

	public void evaluateWindow(IWindowAPI api);

}
