package uk.ac.imperial.lsds.seep.multi;

public interface IMicroOperatorCode {

	public void processData (WindowBatch windowBatch, IWindowAPI api);

	// public void processData (WindowBatch windowBatch, IWindowAPI api, LocalUnboundedQueryBufferFactory bufferFactory);

	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api);
}
