package uk.ac.imperial.lsds.streamsql.op.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
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

	private TupleSchema outSchema;

	public Projection(Expression[] expressions) {
		this.expressions = expressions;
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
	}

	@SuppressWarnings("unchecked")
	public Projection(Expression expression) {
		this.expressions = (Expression[]) new Expression[] {expression};
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (Expression att : expressions)
			sb.append(att.toString() + " ");
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);		
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		
		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();
		
		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		TupleSchema schema = windowBatch.getSchema();

		int outWindowOffset = 0;
		int byteSizeOfTuple = schema.getByteSizeOfTuple();
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int inWindowStartOffset = startPointers[currentWindow];
			int inWindowEndOffset = endPointers[currentWindow];

			/*
			 * If the window is empty, we skip it 
			 */
			if (inWindowStartOffset != -1) {
				
				startPointers[currentWindow] = outWindowOffset;
				// for all the tuples in the window
				while (inWindowStartOffset <= inWindowEndOffset) {
					for (int i = 0; i < expressions.length; i++) 
						outBuffer.put(expressions[i].evalAsByte(inBuffer, schema, inWindowStartOffset));

					outWindowOffset += outSchema.getByteSizeOfTuple();
					inWindowStartOffset += byteSizeOfTuple;
				}
				endPointers[currentWindow] = outWindowOffset;
			}
		}
		
		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);
		
		api.outputWindowBatchResult(-1, windowBatch);
	}


}
