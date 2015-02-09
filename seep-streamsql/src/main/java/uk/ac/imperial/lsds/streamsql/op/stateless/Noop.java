package uk.ac.imperial.lsds.streamsql.op.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;

public class Noop implements IMicroOperatorCode {

	public Noop () {
		/* */
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		/* Copy input to output */
		windowBatch.getBuffer().appendBytesTo(windowBatch.getBatchStartPointer(), windowBatch.getBatchEndPointer(), outputBuffer.array());
		windowBatch.setBuffer(outputBuffer);
		api.outputWindowBatchResult(-1, windowBatch);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("NOOP is single input operator and does not operate on two streams");
		
	}
}
