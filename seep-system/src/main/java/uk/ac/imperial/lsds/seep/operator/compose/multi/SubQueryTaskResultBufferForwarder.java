package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ResultHandler;

public class SubQueryTaskResultBufferForwarder implements ISubQueryTaskResultForwarder {
	
	private SubQueryBufferWindowWrapper bw;
	
	private ResultHandler resultHandler;

	public SubQueryTaskResultBufferForwarder(SubQueryBufferWindowWrapper bw) {
		this.bw = bw;
		this.resultHandler = new ResultHandler();
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		
		for (MultiOpTuple t : result) 
			bw.addToBuffer(t);
	}

	@Override
	public ResultHandler getResultHandler() {
		return this.resultHandler;
	}

}
