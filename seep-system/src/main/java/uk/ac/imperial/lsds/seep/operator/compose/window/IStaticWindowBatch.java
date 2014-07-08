package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IStaticWindowBatch extends IWindowBatch {

	public void registerWindow(List<DataTuple> window);
	
}
