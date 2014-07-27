package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskResultBufferForwarder implements ISubQueryTaskResultForwarder {
	
	private ISubQueryConnectable subQueryConnectable;
	
	public SubQueryTaskResultBufferForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		// Update the buffer with the result
		for (SubQueryBuffer b : subQueryConnectable.getLocalDownstreamBuffers().values()) {
			MultiOpTuple[] notAdded = b.add(result);
			while (notAdded.length == 0) {
				try {
					b.wait();
					notAdded = b.add(notAdded);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
