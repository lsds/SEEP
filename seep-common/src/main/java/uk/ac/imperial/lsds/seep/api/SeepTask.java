package uk.ac.imperial.lsds.seep.api;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface SeepTask {
	
	public void setUp();
	public void processData(DataTuple data);
	public void processData(List<DataTuple> dataList);
	public void close();
	
}
