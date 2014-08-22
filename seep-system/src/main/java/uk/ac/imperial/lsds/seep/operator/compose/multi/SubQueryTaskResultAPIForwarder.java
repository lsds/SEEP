package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ResultHandler;

public class SubQueryTaskResultAPIForwarder implements ISubQueryTaskResultForwarder {

	private MultiOperator multiOperator;
	
	private ResultHandler resultHandler;

	public SubQueryTaskResultAPIForwarder(MultiOperator multiOperator) {
		this.multiOperator = multiOperator;
		this.resultHandler = new ResultHandler();
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		
		/*
		 *  Send the result using the API of the parent MultiOperator
		 */
		for (MultiOpTuple tuple : result) {
			this.multiOperator.getAPI().send(tuple);
		}
	}

	@Override
	public ResultHandler getResultHandler() {
		return this.resultHandler;
	}

}
