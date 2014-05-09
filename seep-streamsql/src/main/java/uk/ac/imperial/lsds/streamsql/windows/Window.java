package uk.ac.imperial.lsds.streamsql.windows;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public interface Window {

	public void updateWindow(DataTuple tuple);
	
	public void updateWindow(List<DataTuple> tuples);
	
	public void registerCallback(WindowOperator operator);
	
}
