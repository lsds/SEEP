package uk.ac.imperial.lsds.seep.operator.compose;

import java.io.Serializable;
import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface SubOperatorAPI extends Serializable{

	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectSubOperatorTo(int localStreamId, SubOperator so);
	
	public void setUp();
	public void processData(DataTuple data);
	public void processData(ArrayList<DataTuple> dataList);
	
}
