package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

public interface IWindowAPI {

	public void outputWindowResult(MultiOpTuple[] windowResult);

	public void outputWindowBatchResult(MultiOpTuple[][] windowBatchResult);

	public void outputWindowResult(int streamID, MultiOpTuple[] windowResult);

	public void outputWindowBatchResult(int streamID, MultiOpTuple[][] windowBatchResult);

}
