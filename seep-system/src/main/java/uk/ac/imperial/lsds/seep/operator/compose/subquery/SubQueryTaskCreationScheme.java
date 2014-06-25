package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Iterator;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface SubQueryTaskCreationScheme extends Iterator<SubQueryTask> {
	
	public void init(ISubQueryConnectable subQueryConnectable, DataTuple lastProcessed);
	
}
