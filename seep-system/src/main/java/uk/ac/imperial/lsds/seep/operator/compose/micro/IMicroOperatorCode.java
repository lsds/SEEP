package uk.ac.imperial.lsds.seep.operator.compose.micro;

import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowAPI;

public interface IMicroOperatorCode {

	public void processData(IWindowBatch window, WindowAPI api);

}
