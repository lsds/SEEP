package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskResultAPIForwarder implements ISubQueryTaskResultForwarder {

	private ISubQueryConnectable subQueryConnectable;
	
	public SubQueryTaskResultAPIForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
	}
	
	@Override
	public void forwardResult(List<DataTuple> result) {
		
		/*
		 *  Send the result using the API of the parent MultiOperator
		 */
		for (DataTuple tuple : result) {
			this.subQueryConnectable.getParentMultiOperator().getAPI().send(tuple);
		}
	}

}
