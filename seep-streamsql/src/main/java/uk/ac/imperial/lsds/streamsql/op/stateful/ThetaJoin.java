package uk.ac.imperial.lsds.streamsql.op.stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
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

		int firstCurrentIndex = firstWindowBatch.getBatchStartPointer();
		int secondCurrentIndex = secondWindowBatch.getBatchStartPointer();

		int firstEndIndex = firstWindowBatch.getBatchEndPointer();
		int secondEndIndex = secondWindowBatch.getBatchEndPointer();
		
		int firstCurrentWindowStart = firstCurrentIndex;
		int firstCurrentWindowEnd = firstCurrentIndex;
		int secondCurrentWindowStart = secondCurrentIndex;
		int secondCurrentWindowEnd = secondCurrentIndex;
		
		IQueryBuffer firstInBuffer = firstWindowBatch.getBuffer();
		IQueryBuffer secondInBuffer = secondWindowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema firstInSchema = firstWindowBatch.getSchema();
		ITupleSchema secondInSchema = secondWindowBatch.getSchema();

		int firstByteSizeOfInTuple = firstInSchema.getByteSizeOfTuple();
		int secondByteSizeOfInTuple = secondInSchema.getByteSizeOfTuple();

		WindowDefinition firstWindowDefinition = firstWindowBatch.getWindowDefinition();
		WindowDefinition secondWindowDefinition = secondWindowBatch.getWindowDefinition();

		long firstCurrentIndexTime;
		long firstStartTime;
		long secondCurrentIndexTime;
		long secondStartTime;
		
		if (outSchema == null)
			outSchema = ExpressionsUtil.mergeTupleSchemas(firstInSchema,
					secondInSchema);
		
		/*
		 * TEMPORARY:
		 * arrays that hold for each tuple in the first batch, the start and end indices of the range 
		 * of tuples in the second batch, against which the join predicate should be evaluated 
		 */
		int[] __startPointersInSecond = new int[(firstEndIndex - firstCurrentIndex)/firstByteSizeOfInTuple];
		int[] __endPointersInSecond = new int[(firstEndIndex - firstCurrentIndex)/firstByteSizeOfInTuple];
		int __firstTupleIndex = 0;

		while (firstCurrentIndex < firstEndIndex || secondCurrentIndex < secondEndIndex) {

			/*
			 * Get timestamps of currently processed tuples in either batch
			 */
			firstCurrentIndexTime = firstWindowBatch.getLong(firstCurrentIndex, 0);
			secondCurrentIndexTime = secondWindowBatch.getLong(secondCurrentIndex, 0);

			/*
			 * Move in first batch?
			 */
			if (firstCurrentIndexTime < secondCurrentIndexTime 
					|| (firstCurrentIndexTime == secondCurrentIndexTime && secondCurrentIndex >= secondEndIndex)) {
				
				/*
				 * TEMPORARY:
				 * set the start and end of the range in the second batch based on the 
				 * current content of the window over the second batch (current meaning at 
				 * the point in time the respective tuple of the first batch is processed). 
				 * Note that the end of this range may later be overwritten when processing
				 * further tuples of the second batch.
				 */
				__startPointersInSecond[__firstTupleIndex] = secondCurrentWindowStart;
				__endPointersInSecond[__firstTupleIndex] = secondCurrentWindowEnd;
				__firstTupleIndex++;
				
				
				/*
				 * Scan second window
				 */
				for (int i = secondCurrentWindowStart; i < secondCurrentWindowEnd; i += secondByteSizeOfInTuple) {
					if (predicate.satisfied(firstInBuffer, firstInSchema,
							firstCurrentIndex, secondInBuffer,
							secondInSchema, i)) {
						firstInBuffer.appendBytesTo(
								firstCurrentIndex, firstByteSizeOfInTuple, outBuffer);
						secondInBuffer.appendBytesTo(
								i, secondByteSizeOfInTuple, outBuffer);
						
						// write dummy content if needed
						outBuffer.put(outSchema.getDummyContent());
					}
				}
				
				/*
				 * Add current tuple to window over first batch
				 */
				firstCurrentWindowEnd = firstCurrentIndex;

				/*
				 * Remove old tuples in window over first batch
				 */
				if (firstWindowDefinition.isRowBased()) {
					if ((firstCurrentWindowEnd - firstCurrentWindowStart)/firstByteSizeOfInTuple > firstWindowDefinition.getSize()) 
						firstCurrentWindowStart += firstWindowDefinition.getSlide() * firstByteSizeOfInTuple;
				}
				else if (firstWindowDefinition.isRangeBased()) {
					firstStartTime = firstWindowBatch.getLong(firstCurrentWindowStart, 0);
					while (firstStartTime < firstCurrentIndexTime - firstWindowDefinition.getSize()) {
						firstCurrentWindowStart += firstByteSizeOfInTuple;
						firstStartTime = firstWindowBatch.getLong(firstCurrentWindowStart, 0);
					}
				}
				/*
				 * Remove old tuples in window over second batch (only for range windows)
				 */
				if (secondWindowDefinition.isRangeBased()) {
					secondStartTime = secondWindowBatch.getLong(secondCurrentWindowStart, 0);
					while (secondStartTime < firstCurrentIndexTime - secondWindowDefinition.getSize()) {
						secondCurrentWindowStart += secondByteSizeOfInTuple;
						secondStartTime = secondWindowBatch.getLong(secondCurrentWindowStart, 0);
					}
				}
				
				/*
				 * Do the actual move in first window batch
				 */
				firstCurrentIndex += firstByteSizeOfInTuple;
			}
			/*
			 * Move in second batch!
			 */
			else {
				
				/*
				 * TEMPORARY:
				 * Override end pointers for range in second window batch for all tuples that 
				 * are in the current window of the first batch. 
				 */
				for (int i = firstCurrentWindowStart; i <= firstCurrentWindowEnd; i += firstByteSizeOfInTuple) {
					int __tmpIndex = (i - firstWindowBatch.getBatchStartPointer()) / firstByteSizeOfInTuple;
					__endPointersInSecond[__tmpIndex] = secondCurrentIndex;
				}
				
				/*
				 * Scan first window
				 */
				for (int i = firstCurrentWindowStart; i < firstCurrentWindowEnd; i += firstByteSizeOfInTuple) {
					if (predicate.satisfied(firstInBuffer, firstInSchema,
							i, secondInBuffer,
							secondInSchema, secondCurrentIndex)) {
						firstInBuffer.appendBytesTo(
								i, firstByteSizeOfInTuple, outBuffer);
						secondInBuffer.appendBytesTo(
								secondCurrentIndex, secondByteSizeOfInTuple, outBuffer);
						
						// write dummy content if needed
						outBuffer.put(outSchema.getDummyContent());

					}
				}
				
				/*
				 * Add current tuple to window over second batch
				 */
				secondCurrentWindowEnd = secondCurrentIndex;

				/*
				 * Remove old tuples in window over second batch
				 */
				if (secondWindowDefinition.isRowBased()) {
					if ((secondCurrentWindowEnd - secondCurrentWindowStart)/secondByteSizeOfInTuple > secondWindowDefinition.getSize()) 
						secondCurrentWindowStart += secondWindowDefinition.getSlide() * secondByteSizeOfInTuple;
				}
				else if (secondWindowDefinition.isRangeBased()) {
					secondStartTime = secondWindowBatch.getLong(secondCurrentWindowStart, 0);
					while (secondStartTime < secondCurrentIndexTime - secondWindowDefinition.getSize()) {
						secondCurrentWindowStart += secondByteSizeOfInTuple;
						secondStartTime = secondWindowBatch.getLong(secondCurrentWindowStart, 0);
					}
				}
				/*
				 * Remove old tuples in window over first batch (only for range windows)
				 */
				if (firstWindowDefinition.isRangeBased()) {
					firstStartTime = firstWindowBatch.getLong(firstCurrentWindowStart, 0);
					while (firstStartTime < secondCurrentIndexTime - firstWindowDefinition.getSize()) {
						firstCurrentWindowStart += firstByteSizeOfInTuple;
						firstStartTime = firstWindowBatch.getLong(firstCurrentWindowStart, 0);
					}
				}
				
				/*
				 * Do the actual move in second window batch
				 */
				secondCurrentIndex += secondByteSizeOfInTuple;
			}
		}
		
		// release old buffers (will return Unbounded Buffers to the pool)
		firstInBuffer.release();
		secondInBuffer.release();

		// reuse the first window batch by setting the new buffer and the new
		// schema for the data in this buffer
		firstWindowBatch.setBuffer(outBuffer);
		firstWindowBatch.setSchema(outSchema);

		// reset window pointers
		firstWindowBatch.setWindowStartPointers(new int[] {0});
		firstWindowBatch.setWindowEndPointers(new int[] {outBuffer.position() - outSchema.getByteSizeOfTuple()});

		api.outputWindowBatchResult(-1, firstWindowBatch);

	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ThetaJoin is multi input operator and does not operator on a single stream");
	}
}
