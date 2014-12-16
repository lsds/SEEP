package uk.ac.imperial.lsds.streamsql.op.stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ThetaJoin implements IStreamSQLOperator, IMicroOperatorCode {

	private static Logger	LOG			= LoggerFactory
												.getLogger(ThetaJoin.class);
	private IPredicate		predicate;

	private ITupleSchema	outSchema	= null;

	public ThetaJoin(IPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {

		int[] firstStartPointers = firstWindowBatch.getWindowStartPointers();
		int[] firstEndPointers = firstWindowBatch.getWindowEndPointers();

		int[] secondStartPointers = secondWindowBatch.getWindowStartPointers();
		int[] secondEndPointers = secondWindowBatch.getWindowEndPointers();

		assert (firstStartPointers.length == secondStartPointers.length);

		IQueryBuffer firstInBuffer = firstWindowBatch.getBuffer();
		IQueryBuffer secondInBuffer = secondWindowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema firstInSchema = firstWindowBatch.getSchema();
		ITupleSchema secondInSchema = secondWindowBatch.getSchema();

		int firstByteSizeOfInTuple = firstInSchema.getByteSizeOfTuple();
		int secondByteSizeOfInTuple = secondInSchema.getByteSizeOfTuple();

		int firstInWindowStartOffset;
		int firstInWindowEndOffset;
		int secondInWindowStartOffset;
		int secondInWindowEndOffset;

		int currentOutPos = 0;

		for (int currentWindow = 0; currentWindow < firstStartPointers.length; currentWindow++) {
			firstInWindowStartOffset = firstStartPointers[currentWindow];
			firstInWindowEndOffset = firstEndPointers[currentWindow];
			secondInWindowStartOffset = secondStartPointers[currentWindow];
			secondInWindowEndOffset = secondEndPointers[currentWindow];

			/*
			 * If the window is empty, we skip it
			 */
			if (firstInWindowStartOffset != -1
					|| secondInWindowStartOffset != -1) {

				/*
				 * For every tuple in window of first window batch
				 */
				while (firstInWindowStartOffset <= firstInWindowEndOffset) {

					/*
					 * For every tuple in window of second window batch
					 */
					while (secondInWindowStartOffset <= secondInWindowEndOffset) {

						if (predicate.satisfied(firstInBuffer, firstInSchema,
								firstInWindowStartOffset, secondInBuffer,
								secondInSchema, secondInWindowStartOffset)) {
							firstInBuffer.appendBytesTo(
									firstInWindowStartOffset,
									firstByteSizeOfInTuple, outBuffer);
							secondInBuffer.appendBytesTo(
									secondInWindowStartOffset,
									secondByteSizeOfInTuple, outBuffer);
						}

						secondInWindowStartOffset += secondByteSizeOfInTuple;
					}

					firstInWindowStartOffset += firstByteSizeOfInTuple;
				}
			}

			if (currentOutPos == outBuffer.position()) {
				firstStartPointers[currentWindow] = -1;
				firstEndPointers[currentWindow] = -1;
			} else {
				firstStartPointers[currentWindow] = currentOutPos;
				firstEndPointers[currentWindow] = outBuffer.position() - 1;
				currentOutPos = outBuffer.position();
			}
		}

		// release old buffers (will return Unbounded Buffers to the pool)
		firstInBuffer.release();
		secondInBuffer.release();

		// reuse the first window batch by setting the new buffer and the new
		// schema for the data in this buffer
		firstWindowBatch.setBuffer(outBuffer);
		if (outSchema == null)
			outSchema = ExpressionsUtil.mergeTupleSchemas(firstInSchema,
					secondInSchema);
		firstWindowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, firstWindowBatch);

	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ThetaJoin is multi input operator and does not operator on a single stream");
	}
}
