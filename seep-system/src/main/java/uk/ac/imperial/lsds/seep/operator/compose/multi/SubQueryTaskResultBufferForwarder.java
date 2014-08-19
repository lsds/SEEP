package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskResultBufferForwarder implements ISubQueryTaskResultForwarder {
	
	private ISubQueryConnectable subQueryConnectable;
	
	private boolean singleDownstreamBuffer;
	
	public SubQueryTaskResultBufferForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
		singleDownstreamBuffer = (subQueryConnectable.getLocalDownstreamBuffers().size() == 1);
	}

	@Override
	public void forwardResult(MultiOpTuple[] result) {
		// Update the buffer with the result
		for (SubQueryBufferWrapper bw : subQueryConnectable.getLocalDownstreamBuffers().values()) {
			// Make sure to copy elements if there is more than one downstream buffer
			if (!singleDownstreamBuffer) {
				MultiOpTuple[] copy = new MultiOpTuple[result.length];
				for (int i = 0; i < result.length; i++)
					copy[i] = MultiOpTuple.newInstance(result[i]);
				result = copy;
			}
			
			MultiOpTuple[] notAdded = bw.add(result);
			while (notAdded.length == 0) {
				try {
					bw.getBuffer().wait();
					notAdded = bw.add(notAdded);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
