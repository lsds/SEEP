package uk.ac.imperial.lsds.seep.operator.compose;

import java.io.Serializable;
import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.LocalApi;

public interface SubOperatorCode extends Serializable{

	public LocalApi api = LocalApi.getInstance();
	
	public void setUp();
	public void processData(DataTuple data);
	public void processData(ArrayList<DataTuple> dataList);
	
}
