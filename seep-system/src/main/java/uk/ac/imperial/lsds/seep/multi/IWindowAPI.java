package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.tmp.Pane;


public interface IWindowAPI {

	public void outputWindowBatchResult(int streamID, WindowBatch windowBatchResult);
	
	public void outputPaneResult (long id, Pane p);

	void outputWindowResult(long windowId, int freeIndex, IQueryBuffer buffer);
}
