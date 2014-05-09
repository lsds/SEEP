package uk.ac.imperial.lsds.streamsql.operator;

import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface WindowOperator {

	public void evaluateWindow(Queue<DataTuple> dataList);

	
}
