package uk.ac.imperial.lsds.seep.multi;

public interface IJoinMicroOperatorCode {

	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api);
}
