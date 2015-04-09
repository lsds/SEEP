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
		
		System.out.println(String.format("[DBG] task %6d 1st window [%10d, %10d] 2nd window [%10d, %10d]", 
				firstWindowBatch.getTaskId(), firstCurrentIndex, firstEndIndex, secondCurrentIndex, secondEndIndex));
		
		__computePointers(firstWindowBatch, secondWindowBatch);
		
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
		 * Is one of the windows empty?
		 */
		if (firstCurrentIndex != firstEndIndex && secondCurrentIndex != secondEndIndex) {
		
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
					for (int i = firstCurrentWindowStart; i <= (firstCurrentWindowEnd - firstByteSizeOfInTuple); i += firstByteSizeOfInTuple) {
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
			
//			System.out.println(Arrays.toString(__startPointersInSecond));
//			System.out.println(Arrays.toString(__endPointersInSecond));

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
		firstWindowBatch.setWindowEndPointers(new int[] {outBuffer.position()});

		api.outputWindowBatchResult(-1, firstWindowBatch);

	}

	private void __computePointers(WindowBatch batch1,
			WindowBatch batch2) {
		
		int currentIndex1 = batch1.getBatchStartPointer();
		int currentIndex2 = batch2.getBatchStartPointer();

		int endIndex1 = batch1.getBatchEndPointer();
		int endIndex2 = batch2.getBatchEndPointer();
		
		int currentWindowStart1 = currentIndex1;
		int currentWindowStart2 = currentIndex2;
		
		int currentWindowEnd1 = currentIndex1;
		int currentWindowEnd2 = currentIndex2;

		ITupleSchema schema1 = batch1.getSchema();
		ITupleSchema schema2 = batch2.getSchema();

		int tupleSize1 = schema1.getByteSizeOfTuple();
		int tupleSize2 = schema2.getByteSizeOfTuple();

		WindowDefinition windowDef1 = batch1.getWindowDefinition();
		WindowDefinition windowDef2 = batch2.getWindowDefinition();

		long currentIndexTime1;
		long startTime1;
		
		long currentIndexTime2;
		long startTime2;
		
		int ntuples1 = (endIndex1 - currentIndex1) / tupleSize1;
		
		int [] __startPointers = new int[ntuples1];
		int []   __endPointers = new int[ntuples1];
		
		/*
		 * Is one of the windows empty?
		 */
		if (currentIndex1 == endIndex1 || currentIndex2 == endIndex2) {
			System.err.println("warning: empty window");
			return;
		}
		
		int __firstTupleIndex = 0;
		
		while (currentIndex1 < endIndex1 || currentIndex2 < endIndex2) {
	
			/*
			 * Get timestamps of currently processed tuples in either batch
			 */
			currentIndexTime1 = batch1.getLong(currentIndex1, 0);
			currentIndexTime2 = batch2.getLong(currentIndex2, 0);
	
			/*
			 * Move in first batch?
			 */
			if (currentIndexTime1 < currentIndexTime2 
					|| (currentIndexTime1 == currentIndexTime2 && currentIndex2 >= endIndex2)) {
					
				__startPointers[__firstTupleIndex] = currentWindowStart2;
				  __endPointers[__firstTupleIndex] = currentWindowEnd2;
				  
				__firstTupleIndex++;
					
				
				/* Add current tuple to window over first batch */
				currentWindowEnd1 = currentIndex1;
	
				/* Remove old tuples in window over first batch */
				if (windowDef1.isRowBased()) {
					
					if ((currentWindowEnd1 - currentWindowStart1)/tupleSize1 > windowDef1.getSize()) 
						currentWindowStart1 += windowDef1.getSlide() * tupleSize1;
					
				} else 
				if (windowDef1.isRangeBased()) {
					
					startTime1 = batch1.getLong(currentWindowStart1, 0);
					
					while (startTime1 < currentIndexTime1 - windowDef1.getSize()) {
						
						currentWindowStart1 += tupleSize1;
						startTime1 = batch1.getLong(currentWindowStart1, 0);
					}
				}
				
				/* Remove old tuples in window over second batch (only for range windows) */
				if (windowDef2.isRangeBased()) {
					
					startTime2 = batch2.getLong(currentWindowStart2, 0);
					
					while (startTime2 < currentIndexTime1 - windowDef2.getSize()) {
						
						currentWindowStart2 += tupleSize2;
						startTime2 = batch2.getLong(currentWindowStart2, 0);
					}
				}
					
				/* Do the actual move in first window batch */
				currentIndex1 += tupleSize1;
				
			} else { /* Move in second batch! */
				
				for (int i = currentWindowStart1; i <= (currentWindowEnd1-tupleSize1); i += tupleSize1) {
					int __tmpIndex = (i - batch1.getBatchStartPointer()) / tupleSize1;
					__endPointers[__tmpIndex] = currentIndex2;
				}
				
				/* Add current tuple to window over second batch */
				currentWindowEnd2 = currentIndex2;
				
				/* Remove old tuples in window over second batch */
				if (windowDef2.isRowBased()) {
					
					if ((currentWindowEnd2 - currentWindowStart2)/tupleSize2 > windowDef2.getSize()) 
						currentWindowStart2 += windowDef2.getSlide() * tupleSize2;
					
				} else 
				if (windowDef2.isRangeBased()) {
					
					startTime2 = batch2.getLong(currentWindowStart2, 0);
					
					while (startTime2 < currentIndexTime2 - windowDef2.getSize()) {
						
						currentWindowStart2 += tupleSize2;
						startTime2 = batch2.getLong(currentWindowStart2, 0);
					}
				}
				
				/* Remove old tuples in window over first batch (only for range windows) */
				if (windowDef1.isRangeBased()) {
					
					startTime1 = batch1.getLong(currentWindowStart1, 0);
					
					while (startTime1 < currentIndexTime2 - windowDef1.getSize()) {
						
						currentWindowStart1 += tupleSize1;
						startTime1 = batch1.getLong(currentWindowStart1, 0);
					}
				}
					
				/* Do the actual move in second window batch */
				currentIndex2 += tupleSize2;
			}
		}
		
		/* Print start and end pointers */
		for (int i = 0; i < ntuples1; i++) {
			System.out.println(String.format("1st batch tuple %6d 2nd batch window [%10d, %10d]", 
					i, __startPointers[i], __endPointers[i]));
		}
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ThetaJoin is multi input operator and does not operator on a single stream");
	}
}
