package uk.ac.imperial.lsds.seep.operator.compose.micro;

import uk.ac.imperial.lsds.seep.operator.compose.multi.WindowAPI;

public interface IMicroOperatorCode {

	public void processData(IWindowBatch window, WindowAPI api);

}
