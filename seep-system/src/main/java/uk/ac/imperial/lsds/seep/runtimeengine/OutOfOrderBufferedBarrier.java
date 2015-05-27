package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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
	public synchronized ArrayList<DataTuple> pull_from_barrier() {
		throw new RuntimeException("TODO"); 
	}

	@Override
	public synchronized FailureCtrl purge(FailureCtrl nodeFctrl) {
		throw new RuntimeException("TODO"); 
	}
	
	//Should return the 'total' queue length at index -1,
	//With the other queue lengths at logical input index 0,1 (if a join).
	public synchronized Map<Integer, Integer> sizes() {
		throw new RuntimeException("TODO"); 
	}
	
	@Override
	public int size() {
		throw new RuntimeException("Logic error.");
	}
	
	//TODO: Note the incoming data handler worker
	//could tell us both the upOpId, the upOpOriginalId
	//or even the meander query index.
	public synchronized void push(DataTuple dt, int upOpId)
	{
		throw new RuntimeException("TODO"); 
	}

	public synchronized Map<Integer, Set<Long>> getRoutingConstraints() {
		throw new RuntimeException("TODO");
	}
}
