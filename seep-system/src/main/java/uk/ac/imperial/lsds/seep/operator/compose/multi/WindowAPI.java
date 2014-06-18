package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface WindowAPI {

	public void outputWindowResult(List<DataTuple> windowResult);

	public void outputWindowBatchResult(List<List<DataTuple>> windowBatchResult);

}
