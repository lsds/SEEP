package uk.ac.imperial.lsds.seep.multi;


public interface IWindowAPI {

	public void outputWindowBatchResult(int streamID, WindowBatch windowBatchResult);
	
	public void outputPaneResult (long id, IntermediateMap paneResult);
}
