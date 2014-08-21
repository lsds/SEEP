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

public class SubQueryTaskGPUCallable extends AbstractSubQueryTask implements ISubQueryTask {

	private GPUExecutionContext gpu;
	
	private long startTime = 0L;
	private long dt = 0L;
	
	private static final int MAX_SEGMENTS = 100;
	
	private int hash (int highway, int direction, int segment) {
		return (segment + (MAX_SEGMENTS * direction)) * (highway + 1);
	}
	
	private int getHighway () {
		return 0;
	}
	
	private int getDirection (int key) {
		return ((key >= MAX_SEGMENTS) ? 1 : 0);
	}
	
	private int getSegment (int key) {
		return (key - (MAX_SEGMENTS * getDirection(key)));
	}
	
	public SubQueryTaskGPUCallable(
			ISubQueryConnectable subQueryConnectable, 
			Map<Integer, IWindowBatch> windowBatches, 
			int logicalOrderID, 
			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices,
			GPUExecutionContext gpu) {
		
		super(subQueryConnectable, windowBatches, logicalOrderID, freeUpToIndices);
		this.gpu = gpu;
	}
	
	@Override
	public void run() {
		
		/*
		 * For the LRB query, we know that there is only a single
		 * input stream
		 */
		startTime = System.currentTimeMillis();
		
		IWindowBatch batch = this.windowBatches.values().iterator().next();
		
		/* Transform data */
		int [] startIndex = batch.getWindowStartPointers();
		int []   endIndex = batch.getWindowEndPointers();
		
		int batchSize = startIndex.length;
		int start = startIndex[0];
		int end =     endIndex[batchSize - 1];
		int totalTuples = end - start + 1;
		
		/* #panes */
		int panes = 2 * batchSize;
		int   [] offsets = new int   [panes];
		int   [] count   = new int   [panes];
		int   [] keys    = new int   [totalTuples];
		int   [] values  = new int   [totalTuples];
		
		Arrays.fill(offsets, -1);
		Arrays.fill(count, -1);
		Arrays.fill(keys, -1);
		Arrays.fill(values, -1);
		
		int segment, highway, direction;
		MultiOpTuple tuple;
		
		int p = 0; /* Current pane */
		int tpp = 1; /* Tuples per pane */
		offsets[p] = 0;
		long  t, _t = batch.getStartTimestamp();
		
		for (int i = 0; i < totalTuples; i++) {
			tuple = batch.get(i + start);
			t = tuple.timestamp;
			highway = 0;
			direction = ((IntegerType) tuple.values[3]).value;
			segment = ((IntegerType) tuple.values[4]).value / 5280;
			keys[i] = hash(highway, direction, segment);
			values[i] = (int) ((FloatType) tuple.values[1]).value;
			/* Populate offsets and count */
			if (t > _t) {
				/* Set current pane & move to next one */
				count[p] = tpp;
				p += 1;
				tpp = 1;
				offsets[p] = i;
			} else {
				/* Inc. current pane count */
				tpp += 1;
			}
			_t = t;
		}
		/* Populate last pane */
		count[p] = tpp;
		
		/* Print pane offsets and count
		for (int i = 0; i < panes; i++) {
			GPUUtils.out(String.format("pane %3d offset %8d count %8d", i, offsets[i], count[i]));
		}
		*/
		
		int resultSize = gpu.getResultSize();
		int [] gpuResults = new int [resultSize];
		
		int taskid = gpu.aggregate(keys, values, offsets, count, gpuResults);
		
		String dbg = String.format(
		"task %3d %3d windows start @%6d end @%6d %8d %8d total %8d %4d panes", 
		taskid, 
		batch.getWindowStartPointers().length, 
		batch.getStartTimestamp(), 
		batch.getEndTimestamp(), 
		start, 
		end, 
		totalTuples,
		p + 1
		);
		GPUUtils.out(dbg);
		
		/*
		 * Store the results of the computation
		 */
		
		dt = System.currentTimeMillis() - startTime;
		gpu.setTaskExecutionTime(dt);
		
		MultiOpTuple [] resultsForWindowBatch = new MultiOpTuple[0];
		
		this.resultStream = resultsForWindowBatch;
	}

}
