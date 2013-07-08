package uk.co.imperial.lsds.seep.operator;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;

public interface OneTupleProcessing {

	public void setUp();

	public void processData(DataTuple dt);
	
}
