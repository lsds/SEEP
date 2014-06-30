package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class SubQueryBuffer extends LinkedBlockingDeque<DataTuple> implements BlockingDeque<DataTuple> {

	private static final long serialVersionUID = 1L;
	
	public SubQueryBuffer(int size) {
		super(size);
	}

}
