package uk.ac.imperial.lsds.streamsql.op.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;

public class LatencyOp implements IMicroOperatorCode {
	
	long count = 0;

	public LatencyOp () {
		/* */
		count = 0L;
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		if (count++ % 100 == 0) {
			IQueryBuffer inputBuffer = windowBatch.getBuffer();
			long _t = inputBuffer.getLong(windowBatch.getBatchStartPointer());
			long t_ = System.nanoTime();
			long dt = t_ - _t;
			// long dt = _t;
			// System.out.println(String.format("Latency %d ns size = %d", dt, windowBatch.getBatchEndPointer() - windowBatch.getBatchStartPointer()));
			System.out.println(String.format("Latency %d ns", dt));
		}
		api.outputWindowBatchResult(-1, windowBatch);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("NOOP is single input operator and does not operate on two streams");
		
	}
}
