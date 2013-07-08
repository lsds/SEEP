package uk.co.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;

public interface DataStructureI {

	public void push(DataTuple dt);
	public DataTuple pull();
	public ArrayList<DataTuple> pull_from_barrier();
	
}
