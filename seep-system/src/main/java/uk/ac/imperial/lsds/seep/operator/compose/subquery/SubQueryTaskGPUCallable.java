package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Arrays;
import java.util.Map;

import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.gpu.GPUUtils;
import uk.ac.imperial.lsds.seep.gpu.types.FloatType;
import uk.ac.imperial.lsds.seep.gpu.types.IntegerType;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

/* 
 * No-op task: selection
 *
 */

public class SubQueryTaskGPUCallable implements ISubQueryTask {
	
	/*
	 * Input data for the actual execution of the task
	 */
	private ISubQueryConnectable subQueryConnectable;
	private Map<Integer, IWindowBatch> windowBatches;
	
	private ResultCollector collector;
	
	private GPUExecutionContext gpu;
	
	private IWindowBatch batch;
	
	private long startTime = 0L;
	private long dt = 0L;
	
	public SubQueryTaskGPUCallable(
			ISubQueryConnectable subQueryConnectable, 
			Map<Integer, IWindowBatch> windowBatches, 
			int logicalOrderID, 
			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices,
			GPUExecutionContext gpu) {
		
		this.subQueryConnectable = subQueryConnectable;
		this.windowBatches= windowBatches;
		this.collector = new ResultCollector(subQueryConnectable, logicalOrderID, freeUpToIndices);
		this.gpu = gpu;
		/*
		 * For the No-op query, we know that there is only a single
		 * input stream
		 */
		batch = this.windowBatches.values().iterator().next();
	}
	
	@Override
	public void run() {
		
		try {
		
		startTime = System.currentTimeMillis();
		
		/* Transform data */
		int [] startIndex = batch.getWindowStartPointers();
		int []   endIndex = batch.getWindowEndPointers();
		int batchSize = startIndex.length;
		int start = startIndex[0];
		int end = endIndex[batchSize - 1];
		int totalTuples = end - start + 1;
		
		/* #panes */
		int panes = batchSize;
		int   [] offsets    = new int   [panes];
		int   [] count      = new int   [panes];
		float [] attribute1 = new float [totalTuples];
		
		Arrays.fill(offsets, -1);
		Arrays.fill(count, -1);
		Arrays.fill(attribute1, -1);
		
		MultiOpTuple tuple;
		
		int p = 0; /* Current pane */
		int tpp = 1; /* Tuples per pane */
		offsets[p] = 0;
		long  t, _t = batch.getStartTimestamp();
		
		for (int i = 0; i < totalTuples; i++) {
			tuple = batch.get(i + start);
			t = tuple.timestamp;
			attribute1[i] = ((FloatType) tuple.values[1]).value;
			/* Populate offsets and count */
			if (t > _t) {
				/* Set current pane & move to next one */
				count[p] = tpp;
				p += 1;
				tpp = 1;
				if (p < panes)
					offsets[p] = i;
			} else {
				/* Inc. current pane count */
				tpp += 1;
			}
			_t = t;
		}
		/* Populate last pane */
		count[p - 1] = tpp;
		
		/* Print pane offsets and count
		for (int i = 0; i < panes; i++) {
			GPUUtils.out(String.format("pane %3d offset %8d count %8d", i, offsets[i], count[i]));
		} */
		
		int S = gpu.getResultSize();
		float [] results = new float [S];
		int taskid = gpu.noop(attribute1, offsets, count, results);
		
		if (taskid % 100 == 0)
			gpu.stats();
		
		/*
		String dbg = String.format(
		"task %3d %3d windows start @%6d end @%6d %8d %8d total %8d %4d panes", 
		taskid, 
		batch.getWindowStartPointers().length, 
		batch.getStartTimestamp(), 
		batch.getEndTimestamp(), 
		start, 
		end, 
		totalTuples,
		p
		);
		GPUUtils.out(dbg);
		*/
		
		/*
		 * Store the results of the computation
		 */
		
		dt = System.currentTimeMillis() - startTime;
		gpu.setTaskExecutionTime(dt);
		
		MultiOpTuple [] resultsForWindowBatch = new MultiOpTuple[0];
		
		this.collector.pushResults(resultsForWindowBatch);
		this.subQueryConnectable.getTaskDispatcher().taskFinished();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

