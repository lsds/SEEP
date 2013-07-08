package uk.co.imperial.lsds.seep.operator;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;

public interface AfterBarrierProcessing {

	public void setUp();
	
	public void processData(ArrayList<DataTuple> ldt);
}
