package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;

public class OutOfOrderBufferedBarrier implements DataStructureI {

	@Override
	public void push(DataTuple dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataTuple pull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FailureCtrl purge(FailureCtrl nodeFctrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
