package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ResultHandler;



public interface ISubQueryTaskResultForwarder {

	public void forwardResult(MultiOpTuple[] result);

	public ResultHandler getResultHandler();
	
}
