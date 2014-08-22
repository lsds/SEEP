package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ResultHandler;

public class SubQueryTaskResultBufferForwarder implements ISubQueryTaskResultForwarder {
	
	private ISubQueryConnectable subQueryConnectable;
	
	private boolean singleDownstreamBuffer;
	
	private ResultHandler resultHandler;

	public SubQueryTaskResultBufferForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
		singleDownstreamBuffer = (subQueryConnectable.getLocalDownstreamBuffers().size() == 1);
		this.resultHandler = new ResultHandler();
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		// Make sure to copy elements if there is more than one downstream buffer
		if (!singleDownstreamBuffer) {
			MultiOpTuple[] copy = new MultiOpTuple[result.length];
			for (int i = 0; i < result.length; i++)
				copy[i] = MultiOpTuple.newInstance(result[i]);
			result = copy;
		}
		for (MultiOpTuple t : result) 
			subQueryConnectable.processData(t);
	}

	@Override
	public ResultHandler getResultHandler() {
		return this.resultHandler;
	}

}
