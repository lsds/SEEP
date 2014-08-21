package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.MultiAPI;

public class SubQueryTaskResultAPIForwarder implements ISubQueryTaskResultForwarder {

	private MultiAPI api;
	
	private ResultHandler resultHandler;

	public SubQueryTaskResultAPIForwarder(MultiAPI api) {
		this.api = api;
		this.resultHandler = new ResultHandler();
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		
		/*
		 *  Send the result using the API of the parent MultiOperator
		 */
		for (MultiOpTuple tuple : result) {
			this.api.send(tuple);
		}
	}

	@Override
	public ResultHandler getResultHandler() {
		return this.resultHandler;
	}

}
