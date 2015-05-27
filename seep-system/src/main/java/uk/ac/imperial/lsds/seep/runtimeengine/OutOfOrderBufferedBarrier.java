package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.manet.Query;

public class OutOfOrderBufferedBarrier implements DataStructureI {

	private final Query meanderQuery;

	public OutOfOrderBufferedBarrier(Query meanderQuery)
	{
		this.meanderQuery = meanderQuery;	//To get logical index for upstreams.
	}
	
	@Override
	public void push(DataTuple dt) {
		throw new RuntimeException("Logic error - use push(DataTuple, int)");
	}

	@Override
	public DataTuple pull() {
		throw new RuntimeException("Logic error - use pull_from_barrier()");
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
	
	//TODO: Note the incoming data handler worker
	//could tell us both the upOpId, the upOpOriginalId
	//or even the meander query index.
	public void push(DataTuple dt, int upOpId)
	{
		
	}
}
