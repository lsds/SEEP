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
		windowBatch.setBuffer(outputBuffer);
		api.outputWindowBatchResult(-1, windowBatch);
	}
}
