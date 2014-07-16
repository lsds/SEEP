package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskResultBufferForwarder implements ISubQueryTaskResultForwarder {
	
	private ISubQueryConnectable subQueryConnectable;
	
	public SubQueryTaskResultBufferForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
	}

	@Override
	public void forwardResult(List<DataTuple> result) {
		// Update the buffer with the result
		for (SubQueryBuffer b : subQueryConnectable.getLocalDownstreamBuffers().values()) {
			List<DataTuple> notAdded = b.add(result);
			while (!notAdded.isEmpty()) {
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
