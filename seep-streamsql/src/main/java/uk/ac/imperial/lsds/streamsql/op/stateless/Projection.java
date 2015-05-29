package uk.ac.imperial.lsds.streamsql.op.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Projection implements IStreamSQLOperator, IMicroOperatorCode {

	/*
	 * Expressions for the extended projection
	 */
	private Expression[] expressions;
	
	private ITupleSchema outSchema;
	
	public Projection (Expression [] expressions) {
		this.expressions = expressions;
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
	}
	
	public Projection (Expression expression) {
		this.expressions = new Expression [] { expression };
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (Expression attribute : expressions)
			sb.append(attribute.toString() + " ");
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);		
	}

//	public void processData (WindowBatch windowBatch, IWindowAPI api, LocalUnboundedQueryBufferFactory bufferFactory) {
//		/*
//		 * Make sure the batch is initialised
//		 */
//		windowBatch.initWindowPointers();
////		System.out.println("RUN projection");
//		
//		int [] startPointers = windowBatch.getWindowStartPointers ();
//		int [] endPointers   = windowBatch.getWindowEndPointers ();
//		
//		IQueryBuffer inBuffer  = windowBatch.getBuffer();
//		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
//		
////		System.out.println("projection: task " + windowBatch.getTaskId() + " use buffer " + outBuffer + " position " + outBuffer.position());
////		System.out.println(String.format("[DBG] In projection: ts %d start %10d end %10d", 
////				inBuffer.getLong(windowBatch.getBatchStartPointer()), windowBatch.getBatchStartPointer(), windowBatch.getBatchEndPointer()));
//		
//		if (outBuffer.position() != 0) {
//			System.err.println("error: buffer not reset (" + outBuffer.position() + ")");
//			System.exit(1);
//		}
//		
//		ITupleSchema schema = windowBatch.getSchema();
//		int byteSizeOfTuple = schema.getByteSizeOfTuple();
//		
//		
//		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
//			
//			int inWindowStartOffset = startPointers[currentWindow];
//			int inWindowEndOffset = endPointers[currentWindow];
//			
//			/*
//			 * If the window is empty, skip it 
//			 */
//			if (inWindowStartOffset != -1) {
//				
//				startPointers[currentWindow] = outBuffer.position();
//				/* For all the tuples in the window */
//				while (inWindowStartOffset < inWindowEndOffset) {
//					for (int i = 0; i < expressions.length; i++) {
//						expressions[i].appendByteResult(inBuffer, schema, inWindowStartOffset, outBuffer);
//					}
//					outBuffer.put(outSchema.getDummyContent());
//					inWindowStartOffset += byteSizeOfTuple;
//				}
//				endPointers[currentWindow] = outBuffer.position() - 1;
//			}
//		}
//		
//		/* Return (unbounded) buffers to the pool */
//		inBuffer.release();
//		/* Reuse window batch by setting the new buffer and the new schema for the data in this buffer */
//		// outBuffer.close();
//		windowBatch.setBuffer(outBuffer);
//		windowBatch.setSchema(outSchema);
//		
//		api.outputWindowBatchResult(-1, windowBatch);
//	}

	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		// System.out.println(String.format("[DBG] projection %06d", windowBatch.getTaskId()));
		/*
		 * Make sure the batch is initialised
		 */
		// windowBatch.initWindowPointers();
//		System.out.println("RUN projection");
		
//		int [] startPointers = windowBatch.getWindowStartPointers ();
//		int [] endPointers   = windowBatch.getWindowEndPointers ();
		
		IQueryBuffer inBuffer  = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		
//		System.out.println("projection: task " + windowBatch.getTaskId() + " use buffer " + outBuffer + " position " + outBuffer.position());
//		System.out.println(String.format("[DBG] In projection: ts %d start %10d end %10d", 
//				inBuffer.getLong(windowBatch.getBatchStartPointer()), windowBatch.getBatchStartPointer(), windowBatch.getBatchEndPointer()));
		
		if (outBuffer.position() != 0) {
			System.err.println("error: buffer not reset (" + outBuffer.position() + ")");
			System.exit(1);
		}
		
		ITupleSchema schema = windowBatch.getSchema();
		int byteSizeOfTuple = schema.getByteSizeOfTuple();
		
		
//		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
//			
//			int inWindowStartOffset = startPointers[currentWindow];
//			int inWindowEndOffset = endPointers[currentWindow];
//			
//			/*
//			 * If the window is empty, skip it 
//			 */
//			if (inWindowStartOffset != -1) {
//				
//				startPointers[currentWindow] = outBuffer.position();
//				/* For all the tuples in the window */
//				while (inWindowStartOffset < inWindowEndOffset) {
//					for (int i = 0; i < expressions.length; i++) {
//						expressions[i].appendByteResult(inBuffer, schema, inWindowStartOffset, outBuffer);
//					}
//					outBuffer.put(outSchema.getDummyContent());
//					inWindowStartOffset += byteSizeOfTuple;
//				}
//				endPointers[currentWindow] = outBuffer.position() - 1;
//			}
//		}
		
		for (int p = windowBatch.getBatchStartPointer(); p < windowBatch.getBatchEndPointer(); p += byteSizeOfTuple) {
			for (int i = 0; i < expressions.length; i++) {
				expressions[i].appendByteResult(inBuffer, schema, p, outBuffer);
			}
			outBuffer.put(outSchema.getDummyContent());
		}
		
		/* Return (unbounded) buffers to the pool */
		inBuffer.release();
		/* Reuse window batch by setting the new buffer and the new schema for the data in this buffer */
		// outBuffer.close();
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);
		
//		if (outBuffer.position() > 1048576)
//			System.err.println("buffer position after filling (" + outBuffer.position() + ")");

		api.outputWindowBatchResult(-1, windowBatch);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("Projection is single input operator and does not operate on two streams");
	}
}
