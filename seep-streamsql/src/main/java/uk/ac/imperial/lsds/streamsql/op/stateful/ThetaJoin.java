package uk.ac.imperial.lsds.streamsql.op.stateful;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ThetaJoin implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static boolean computePointers = false;
	private static boolean debug = false;

	private IPredicate predicate;

	private ITupleSchema outSchema = null;
	
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

		int firstCurrentIndex  =  firstWindowBatch.getBatchStartPointer();
		int secondCurrentIndex = secondWindowBatch.getBatchStartPointer();

		int firstEndIndex  = firstWindowBatch.getBatchEndPointer();
		int secondEndIndex = secondWindowBatch.getBatchEndPointer();
		
		int firstCurrentWindowStart = firstCurrentIndex;
		int firstCurrentWindowEnd   = firstCurrentIndex;
		
		int secondCurrentWindowStart = secondCurrentIndex;
		int secondCurrentWindowEnd   = secondCurrentIndex;
		
		IQueryBuffer firstInBuffer  =  firstWindowBatch.getBuffer();
		IQueryBuffer secondInBuffer = secondWindowBatch.getBuffer();
		
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema firstInSchema  =  firstWindowBatch.getSchema();
		ITupleSchema secondInSchema = secondWindowBatch.getSchema();

		int firstByteSizeOfInTuple  =  firstInSchema.getByteSizeOfTuple();
		int secondByteSizeOfInTuple = secondInSchema.getByteSizeOfTuple();

		WindowDefinition firstWindowDefinition  =  firstWindowBatch.getWindowDefinition();
		WindowDefinition secondWindowDefinition = secondWindowBatch.getWindowDefinition();
		
		if (debug) {
			System.out.println(String.format("[DBG] task %6d 1st batch [%10d, %10d] %10d tuples [free %10d] / 2nd batch [%10d, %10d] %10d tuples [free %10d]", 
				firstWindowBatch.getTaskId(), 
				firstCurrentIndex, 
				firstEndIndex,
				(firstEndIndex - firstCurrentIndex)/firstByteSizeOfInTuple,
				firstWindowBatch.getFreeOffset(),
				secondCurrentIndex, 
				secondEndIndex,
				(secondEndIndex - secondCurrentIndex)/secondByteSizeOfInTuple,
				secondWindowBatch.getFreeOffset()
				));
		}
		
		long firstCurrentIndexTime;
		long firstStartTime;
		
		long secondCurrentIndexTime;
		long secondStartTime;
		
		if (outSchema == null)
			outSchema = ExpressionsUtil.mergeTupleSchemas(firstInSchema, secondInSchema);
		
		int ntuples1 = 0;
		int [] __startPointersInSecond = new int[0];
		int []   __endPointersInSecond = new int[0];
		int __firstTupleIndex = 0;
		
		/*
		 * Is one of the windows empty?
		 */
		if (firstCurrentIndex != firstEndIndex && secondCurrentIndex != secondEndIndex) {
		
			/*
			 * Compute pointers:
			 * 
			 * arrays that hold for each tuple in the first batch, the start and end indices of 
			 * the range of tuples in the second batch, against which the join predicate should 
			 * be evaluated.
			 */
			if (computePointers) {
				
				ntuples1 = (firstEndIndex - firstCurrentIndex)/firstByteSizeOfInTuple;
				
				__startPointersInSecond = new int[ntuples1];
				  __endPointersInSecond = new int[ntuples1];
				  
				__firstTupleIndex = 0;
			}
	
			while (firstCurrentIndex < firstEndIndex || secondCurrentIndex < secondEndIndex) {
				/*
				 * Get timestamps of currently processed tuples in either batch
				 */
				firstCurrentIndexTime  = getTimestamp( firstWindowBatch,  firstCurrentIndex, 0);
				secondCurrentIndexTime = getTimestamp(secondWindowBatch, secondCurrentIndex, 0);
				/*
				 * Move in first batch?
				 */
				if (firstCurrentIndexTime < secondCurrentIndexTime 
					|| (firstCurrentIndexTime == secondCurrentIndexTime && secondCurrentIndex >= secondEndIndex)) {
					
					/*
					 * Compute pointers:
					 * 
					 * Set the start and end pointers based on the `current` content of the window 
					 * of the second batch (`current` meaning at the point in time the  respective 
					 * tuple of the first batch is processed). 
					 * 
					 * Note that the end of this range may later be overwritten when processing
					 * further tuples of the second batch.
					 */
					if (computePointers) {
						__startPointersInSecond[__firstTupleIndex] = secondCurrentWindowStart;
						__endPointersInSecond  [__firstTupleIndex] = secondCurrentWindowEnd;
						__firstTupleIndex++;
					}
					
					/*
					 * Scan second window
					 */
					for (int i = secondCurrentWindowStart; i < secondCurrentWindowEnd; i += secondByteSizeOfInTuple) {
						if (
							predicate == null || 
							predicate.satisfied (
								firstInBuffer, firstInSchema, firstCurrentIndex, 
								secondInBuffer, secondInSchema, i
							)
						) {
							 firstInBuffer.appendBytesTo(firstCurrentIndex, firstByteSizeOfInTuple, outBuffer);
							secondInBuffer.appendBytesTo(i, secondByteSizeOfInTuple, outBuffer);
							// Write dummy content if needed
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
						firstStartTime = getTimestamp(firstWindowBatch, firstCurrentWindowStart, 0); 
						while (firstStartTime < firstCurrentIndexTime - firstWindowDefinition.getSize()) {
							firstCurrentWindowStart += firstByteSizeOfInTuple;
							firstStartTime = getTimestamp(firstWindowBatch, firstCurrentWindowStart, 0);
						}
					}
					/*
					 * Remove old tuples in window over second batch (only for range windows)
					 */
					if (secondWindowDefinition.isRangeBased()) {
						secondStartTime = getTimestamp(secondWindowBatch, secondCurrentWindowStart, 0);
						while (secondStartTime < firstCurrentIndexTime - secondWindowDefinition.getSize()) {
							secondCurrentWindowStart += secondByteSizeOfInTuple;
							secondStartTime = getTimestamp(secondWindowBatch, secondCurrentWindowStart, 0);
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
					 * Compute pointers:
					 * 
					 * Override end pointers for range in second window batch for all tuples that 
					 * are in the current window of the first batch.
					 */
					if (computePointers) {
						for (int i = firstCurrentWindowStart; i <= (firstCurrentWindowEnd - firstByteSizeOfInTuple); 
								i += firstByteSizeOfInTuple) {
							
							int __tmpIndex = (i - firstWindowBatch.getBatchStartPointer()) / firstByteSizeOfInTuple;
							__endPointersInSecond[__tmpIndex] = secondCurrentIndex;
						}
					}
					/*
					 * Scan first window
					 */
					for (int i = firstCurrentWindowStart; i < firstCurrentWindowEnd; i += firstByteSizeOfInTuple) {
						if (
							predicate == null || 
							predicate.satisfied (
								firstInBuffer, firstInSchema, i, 
								secondInBuffer, secondInSchema, secondCurrentIndex
							)
						) {
							firstInBuffer.appendBytesTo(i, firstByteSizeOfInTuple, outBuffer);
							secondInBuffer.appendBytesTo(secondCurrentIndex, secondByteSizeOfInTuple, outBuffer);
							// Write dummy content if needed
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
						secondStartTime = getTimestamp(secondWindowBatch, secondCurrentWindowStart, 0);
						while (secondStartTime < secondCurrentIndexTime - secondWindowDefinition.getSize()) {
							secondCurrentWindowStart += secondByteSizeOfInTuple;
							secondStartTime = getTimestamp(secondWindowBatch, secondCurrentWindowStart, 0);
						}
					}
					/*
					 * Remove old tuples in window over first batch (only for range windows)
					 */
					if (firstWindowDefinition.isRangeBased()) {
						firstStartTime = getTimestamp(firstWindowBatch, firstCurrentWindowStart, 0);
						while (firstStartTime < secondCurrentIndexTime - firstWindowDefinition.getSize()) {
							firstCurrentWindowStart += firstByteSizeOfInTuple;
							firstStartTime = getTimestamp(firstWindowBatch, firstCurrentWindowStart, 0);
						}
					}
					/*
					 * Do the actual move in second window batch
					 */
					secondCurrentIndex += secondByteSizeOfInTuple;
				}
			}
			
			/* Print start and end pointers */
			if (debug && computePointers) {
				for (int i = 0; i < ntuples1; i++) {
					System.out.println(String.format("1st batch tuple %6d 2nd batch window [%10d, %10d]", 
						i, __startPointersInSecond[i], __endPointersInSecond[i]));
				}
			}
		}
		/* Release old buffers (return UnboundedBuffer objects to the pool) */
		 firstInBuffer.release();
		secondInBuffer.release();

		/* reuse the first window batch by setting the new buffer
		 * and the new schema for the data in this buffer
		 */
		firstWindowBatch.setBuffer(outBuffer);
		firstWindowBatch.setSchema(outSchema);

		/* Reset window pointers */
		firstWindowBatch.setWindowStartPointers(new int[] { 0 });
		firstWindowBatch.setWindowEndPointers(new int[] { outBuffer.position() });
		
		if (debug) 
			System.out.println("[DBG] output buffer position is " + outBuffer.position());
		
		/* Print tuples
		outBuffer.close();
		int tid = 1;
		while (outBuffer.hasRemaining()) {
		
			System.out.println(String.format("%03d: %2d,%2d,%2d,%2d,%2d,%2d,%2d | %2d,%2d,%2d,%2d,%2d,%2d,%2d", 
			tid++,
			outBuffer.getByteBuffer().getLong(),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getLong(),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt (),
			outBuffer.getByteBuffer().getInt ()
			));
		}
		*/
		api.outputWindowBatchResult(-1, firstWindowBatch);
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}
	
	private long getTimestamp (WindowBatch batch, int index, int attribute) {
		long value = batch.getLong(index, attribute);
		if (Utils.LATENCY_ON)
			return (long) Utils.unpack(0, value);
		else 
			return value;
	}
	
	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ThetaJoin is multi input operator and does not operator on a single stream");
	}
}
