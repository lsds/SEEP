package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IWindowAPI {

	public void outputWindowResult(List<DataTuple> windowResult);

	public void outputWindowBatchResult(List<List<DataTuple>> windowBatchResult);

	public void outputWindowResult(int streamID, List<DataTuple> windowResult);

	public void outputWindowBatchResult(int streamID, List<List<DataTuple>> windowBatchResult);

}
