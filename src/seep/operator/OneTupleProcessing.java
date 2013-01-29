package seep.operator;

import seep.comm.serialization.DataTuple;

public interface OneTupleProcessing {

	public void setUp();

	public void processData(DataTuple dt);
	
}
