package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface ISubQueryTaskResultForwarder {

	public void forwardResult(List<DataTuple> result);
	
}
