package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class WindowBatchTaskCreationScheme implements
		SubQueryTaskCreationScheme {

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	private ISubQueryConnectable subQueryConnectable;
	
	private DataTuple lastProcessed;
	
	private SubQueryBuffer input;
	
	public WindowBatchTaskCreationScheme(){
		
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SubQueryTask next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException("");
	}
	@Override
	public void init(ISubQueryConnectable subQueryConnectable,
			SubQueryBuffer input,
			DataTuple lastProcessed) {
		this.subQueryConnectable = subQueryConnectable;
		this.input = input;
		this.lastProcessed = lastProcessed;
	}

}
