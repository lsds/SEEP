package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.gpu.GPUUtils;

public class SubQueryTaskGPUCallable implements ISubQueryTaskCallable {

	/*
	 * Window batches per streamID
	 */
	private Map<Integer, IWindowBatch> windowBatches;
	
	
	/*
	 * Result of the query for the given window batch
	 */
	private SubQueryTaskResult result;
	
	private GPUExecutionContext gpu;
	
	private static final int capacity = 2000000;
	
	public SubQueryTaskGPUCallable(Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, Map<SubQueryBuffer, Integer> freeUpToIndices,
		GPUExecutionContext gpu) {
		
		this.windowBatches = windowBatches;
		this.result = new SubQueryTaskResult(logicalOrderID, freeUpToIndices);
		
		this.gpu = gpu;
	}
	
	@Override
	public SubQueryTaskResult call() throws Exception {
		
		/*
		 * For the LRB query, we know that there is only a single
		 * input stream
		 */
		IWindowBatch batch = this.windowBatches.values().iterator().next();
		
		/* Transform data */
		int [] startIndex = batch.getWindowStartPointers();
		int []   endIndex = batch.getWindowEndPointers();
		
		int batchSize = startIndex.length;
		int start = startIndex[0];
		int end =     endIndex[batchSize - 1];
		int totalTuples = end - start + 1;
		
		
		
		int taskid = gpu.aggregate();
		
		String dbg = String.format(
		"task %3d %3d windows start @%6d end @%6d %8d %8d total %8d", 
		taskid, batch.getWindowStartPointers().length, batch.getStartTimestamp(), batch.getEndTimestamp(), start, end, totalTuples
		);
		GPUUtils.out(dbg);
		
		/*
		 * Store the results of the computation
		 */
		MultiOpTuple [] resultsForWindowBatch = new MultiOpTuple[0];
		
		this.result.setResultStream(resultsForWindowBatch);
		
		return this.result;
	}
}
