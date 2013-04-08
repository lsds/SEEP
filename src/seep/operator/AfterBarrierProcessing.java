package seep.operator;

import java.util.ArrayList;

import seep.comm.serialization.DataTuple;

public interface AfterBarrierProcessing {

	public void setUp();
	
	public void processData(ArrayList<DataTuple> ldt);
}
