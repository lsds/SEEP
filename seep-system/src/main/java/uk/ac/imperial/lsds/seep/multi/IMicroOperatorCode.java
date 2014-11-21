package uk.ac.imperial.lsds.seep.multi;

import java.util.Map;

public interface IMicroOperatorCode {

	public void processData(WindowBatch windowBatch, IWindowAPI api);
	
}
