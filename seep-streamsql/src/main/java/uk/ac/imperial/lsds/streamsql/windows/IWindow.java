package uk.ac.imperial.lsds.streamsql.windows;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public interface IWindow {

	public void updateWindow(DataTuple tuple);
	
	public void updateWindow(List<DataTuple> tuples);

	public void registerAPI(WindowOperator operator, API api);

	public void registerCallbackEvaluateWindow(WindowOperator operator);

	public void registerCallbackEnterWindow(WindowOperator operator);

	public void registerCallbackExitWindow(WindowOperator operator);

}
