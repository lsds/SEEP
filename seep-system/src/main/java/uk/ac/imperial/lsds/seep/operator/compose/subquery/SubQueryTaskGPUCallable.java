package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;
import java.util.concurrent.Callable;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

public class SubQueryTaskGPUCallable implements Callable<SubQueryTaskResult> {

	/*
	 * Window batches per streamID
	 */
	private Map<Integer, IWindowBatch> windowBatches;
	
	
	/*
	 * Result of the query for the given window batch
	 */
	private SubQueryTaskResult result;
	
	public SubQueryTaskGPUCallable(Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, Map<SubQueryBuffer, Integer> freeUpToIndices) {
		this.windowBatches = windowBatches;
		this.result = new SubQueryTaskResult(logicalOrderID, freeUpToIndices);
	}
	
	@Override
	public SubQueryTaskResult call() throws Exception {
		
		/*
		 * For the LRB query, we know that there is only a single
		 * input stream
		 */
		IWindowBatch batch = this.windowBatches.values().iterator().next();
		
		/*
		 * Definitions of the windows 
		 */
		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();
		
		/*
		 * Assuming that the window batch is defined over an input buffer, 
		 * we get a buffer reference to access the content
		 */
		SubQueryBuffer buffer = batch.getBufferContent();
		
		/*
		 * Do some incremental computation per window
		 */
		int prevWindowStart = 0;
		int prevWindowEnd = 0;
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			/*
			 * Tuples in current window that have not been in the previous window
			 */
			for (int i = prevWindowEnd; i < windowEnd; i++) {
				MultiOpTuple tuple = buffer.get(i);
				// do incremental computation with new tuple
			}

			/*
			 * Tuples in previous window that are not in current window
			 */
			for (int i = prevWindowStart; i < windowStart; i++) {
				MultiOpTuple tuple = buffer.get(i);
				// do incremental computation with outdated tuple
			}

			/*
			 *  compute value for current window
			 */
			
			
		}

		/*
		 * Store the results of the computation
		 */
		MultiOpTuple[] resultsForWindowBatch = new MultiOpTuple[0];
		
		this.result.setResultStream(resultsForWindowBatch);
		
		return this.result;
	}
	
	

	
}
