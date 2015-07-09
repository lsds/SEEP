package uk.ac.imperial.lsds.streamsql.op.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Selection implements IStreamSQLOperator, IMicroOperatorCode {
	
	private boolean selectivity = false;
	
	private long invoked = 0L;
	private long matched = 0L;
	
	private IPredicate predicate;
	
	public Selection(IPredicate predicate) {
		this.predicate = predicate;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	public IPredicate getPredicate() {
		return this.predicate;
	}
	
	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		
		ITupleSchema schema = windowBatch.getSchema();
		int byteSizeOfTuple = schema.getByteSizeOfTuple();
		
		// windowBatch.initWindowPointers();

		// int [] startPointers = windowBatch.getWindowStartPointers();
		// int []   endPointers = windowBatch.getWindowEndPointers();
		
		IQueryBuffer  inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		
		if (outBuffer.position() > 0) {
			System.err.println("error: invalid initial buffer position (" + outBuffer.position() + ")");
			System.exit(1);
		}
		
		// System.out.println("[DBG] task " + windowBatch.getTaskId());
		
		if (selectivity) {
			invoked = 0;
			matched = 0;
		}
		
//		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
//			
//			int inWindowStartOffset = startPointers[currentWindow];
//			int inWindowEndOffset   = endPointers[currentWindow]; // inWindowStartOffset + 32 * 4096; // 256 * 1024; // endPointers[currentWindow];
//			
//			/*
//			 * If the window is empty, skip it 
//			 */
//			if (inWindowStartOffset != -1) {
//				
//				startPointers[currentWindow] = outBuffer.position();
//				/* For all the tuples in the window... */
//				while (inWindowStartOffset < inWindowEndOffset) {
//					if (selectivity)
//						invoked ++;
//					if (this.predicate.satisfied(inBuffer, schema, inWindowStartOffset)) {
//						if (selectivity)
//							matched ++;
//						/* Write tuple to result buffer */
//						if (outBuffer.position() >= outBuffer.capacity()) {
//							System.err.println("error: result buffer overflow");
//							System.exit(1);
//						}
//						inBuffer.appendBytesTo (inWindowStartOffset, byteSizeOfTuple, outBuffer);
//					}
//					/*
//					 * NOTE:
//					 * 
//					 * What is the purpose of the putting the dummy content?
//					 * Don't we copy `byteSizeOfTuple` bytes?
//					 *
//					 * outBuffer.put(schema.getDummyContent());
//					 */
//					inWindowStartOffset += byteSizeOfTuple;
//				}
//				endPointers[currentWindow] = outBuffer.position() - 1;
//			}
//		}
		
		for (int p = windowBatch.getBufferStartPointer(); p < windowBatch.getBufferEndPointer(); p += byteSizeOfTuple) {
			if (selectivity)
				invoked ++;
			if (this.predicate.satisfied(inBuffer, schema, p)) {
				if (selectivity)
					matched ++;
				/* Write tuple to result buffer */
				if (outBuffer.position() >= outBuffer.capacity()) {
					System.err.println("error: result buffer overflow");
					System.exit(1);
				}
				inBuffer.appendBytesTo (p, byteSizeOfTuple, outBuffer);
			}
		}
		
		if (selectivity) {
			if (invoked > 0) {
				System.out.println(String.format("[DBG] [Selection] task %6d batch selectivity is %4.1f%% (%d/%d)", 
						windowBatch.getTaskId(), ((double) matched) * 100D / ((double) invoked), matched, invoked));
			} else {
				System.out.println(String.format("[DBG] [Selection] task %d is empty", windowBatch.getTaskId()));
			}
		}
		
		/* Release old buffer (returns UnboundedBuffer to the pool) */
		inBuffer.release();
		
		/* Reuse window batch, setting the new buffer as its data */
		windowBatch.setBuffer(outBuffer);
		
		api.outputWindowBatchResult(-1, windowBatch);
		
		
//		System.err.println("Disrupted");
//		System.exit(-1);
		
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("Selection is single input operator and does not operate on two streams");
	}
}
