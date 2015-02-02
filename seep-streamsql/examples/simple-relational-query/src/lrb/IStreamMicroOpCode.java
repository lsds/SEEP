package lrb;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;

public class IStreamMicroOpCode implements IMicroOperatorCode {

	LongExpression timestampColumnRef = new LongColumnReference(0);
	
	public IStreamMicroOpCode() {
		
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		int numberOfWindows = windowBatch.getWindowStartPointers().length;
		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();
		
		int startPointer = -1;
		int window = 0;
		while (startPointer == -1 && window < numberOfWindows)
			startPointer = startPointers[window++];
		
		int endPointer = -1;
		window = numberOfWindows - 1;
		while (endPointer == -1 && window >= 0)
			endPointer = endPointers[window--];

		long startTime = timestampColumnRef.eval(inBuffer, inSchema, startPointer);
		long lastTime = startTime;
		long currentTime;
		int  currentHash;
		
		Set<Integer> hashesLastTime = new HashSet<Integer>();
		Set<Integer> hashesCurrentTime = new HashSet<Integer>();

		for (int currentPointer = startPointer; currentPointer < endPointer; currentPointer += byteSizeOfInTuple) {
			currentTime = timestampColumnRef.eval(inBuffer, inSchema, currentPointer);
			
			if (currentTime != lastTime) {
				hashesLastTime = hashesCurrentTime;
				hashesCurrentTime = new HashSet<Integer>();
			}
			
			currentHash = Arrays.hashCode(inBuffer.array(currentPointer + 8, byteSizeOfInTuple - 8));
			
			/*
			 *  Make sure to ignore all the tuples that have the first timestamp 
			 *  that is encountered in the batch, these tuples are only used to
			 *  build up the state of the operator
			 */
			if (currentTime != startTime) {
				/*
				 * The actual istream check: did the tuple occur already at the last time point?
				 */
				if (!hashesLastTime.contains(currentHash)) {
					outBuffer.put(inBuffer, currentPointer, byteSizeOfInTuple);
				}
			}
			hashesCurrentTime.add(currentHash);
			
			
		}
		
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("IStream is a single input operator and does not operate on two streams");
	}
}
