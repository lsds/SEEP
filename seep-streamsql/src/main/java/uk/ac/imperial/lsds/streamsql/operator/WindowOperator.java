package uk.ac.imperial.lsds.streamsql.operator;

import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;

public interface WindowOperator {

	public void enteredWindow(DataTuple tuple, API api);

	public void exitedWindow(DataTuple tuple, API api);

	public void evaluateWindow(Queue<DataTuple> dataList, API api);

	
}
