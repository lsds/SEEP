package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;

public interface IMicroOperatorCode {

	public void processData(Map<Integer, IWindowBatch> windowBatches, IWindowAPI api);

}
