package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCPUCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskGPUCallable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

public class SubQueryTaskDispatcher {
	
	private final Logger LOG = LoggerFactory.getLogger(SubQueryTaskDispatcher.class);

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	/*
	 * Counter for dispatched tasks 
	 */
	private int logicalOrderID = -1;
	
	private ISubQueryConnectable subQueryConnectable;
	
	private boolean isGPUEnabled;
	private GPUExecutionContext gpuContext;
	
	private AtomicInteger finished  = new AtomicInteger(0);
	// public AtomicInteger taskcounter  = new AtomicInteger(0);
	private long target    = 0L;
	
	// private long current_time, previous_time = 0L;
	// private long ntuples, dt;
	// private double rate;
	public AtomicLong num_tasks = new AtomicLong(0L);
	public AtomicLong current_tuplecount = new AtomicLong(0L);
	
	public SubQueryTaskDispatcher(ISubQueryConnectable subQueryConnectable) {

		assert(SUB_QUERY_WINDOW_BATCH_COUNT > 0);
		
		this.subQueryConnectable = subQueryConnectable;
	}
	
	public void setUp() {
		this.target = this.subQueryConnectable.getParentMultiOperator().getTarget();
		System.out.println(String.format("Target is %d tasks", target));
		
		this.isGPUEnabled = this.subQueryConnectable.getParentMultiOperator().isGPUEnabled();
		this.gpuContext = this.subQueryConnectable.getParentMultiOperator().getGPUContext();
		
		Thread measurementThread = new Thread(new DispatcherMeasurement(this, this.subQueryConnectable));
		measurementThread.start();
	}
	
	public void assembleAndDispatchTask(long current_tuplecount) {
		
		this.current_tuplecount.set(current_tuplecount);
		/*
		 * We can assemble a new task if there is at least one fully filled
		 * window batch for all the input streams of this query 
		 */
		boolean canAssemble = true;
		
		for (SubQueryBufferWindowWrapper bufferWrapper : this.subQueryConnectable.getLocalUpstreamBuffers().values())
			canAssemble &= bufferWrapper.getFullWindowBatches().size() > 1;
			
		if (canAssemble) {
			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices = new HashMap<>();
			Map<Integer, IWindowBatch> windowBatchesForStreams = new HashMap<>();

			for (Integer streamID : this.subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
				SubQueryBufferWindowWrapper bufferWrapper = this.subQueryConnectable.getLocalUpstreamBuffers().get(streamID);
				IWindowBatch batch = bufferWrapper.getFullWindowBatches().poll();
//				System.out.println("BATCH for stream "+ streamID + "\t buffer view:\t" + batch.getWindowStartPointers()[0] + "-" + batch.getWindowEndPointers()[batch.getWindowEndPointers().length - 1]+ "\t time:\t" +  batch.getStartTimestamp() + "-" +  batch.getEndTimestamp());
//				System.out.println(batch.getWindowEndPointers()[299] - batch.getWindowStartPointers()[0]);
//				System.out.println(Arrays.toString(batch.getWindowStartPointers()));
//				System.out.println(Arrays.toString(batch.getWindowEndPointers()));
//				System.out.println(bufferWrapper.getFreeIndexForBatchAndRemoveEntry(batch));
				
				int freeUpToIndex = bufferWrapper.getFreeIndexForBatchAndRemoveEntry(batch);
//				System.out.println("In dispatcher, free: " + freeUpToIndex);
				if (freeUpToIndex != -1)
					freeUpToIndices.put(bufferWrapper, freeUpToIndex);
				
				windowBatchesForStreams.put(streamID, batch);
				
			}
			
			/*
			 * Create task 
			 */
			ISubQueryTask task;
			if (this.isGPUEnabled)
				task = new SubQueryTaskGPUCallable(subQueryConnectable, windowBatchesForStreams, freshLogicalOrderID(), freeUpToIndices, this.gpuContext);
			else
				task = new SubQueryTaskCPUCallable(subQueryConnectable, windowBatchesForStreams, freshLogicalOrderID(), freeUpToIndices);
			
			/*
			 * Dispatch task 
			 */
			
			try {
				this.subQueryConnectable.getParentMultiOperator().getExecutorService().submit(task);
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();	
			}
			
			// long dummy_time = System.currentTimeMillis();
			
			//if (num_tasks % 100 == 0) {
			//	current_time = System.currentTimeMillis();
			//	if (previous_time > 0) {
			//		dt = current_time - previous_time;
			//		ntuples = current_tuplecount - previous_tuplecount;
			//		rate = ((double) ntuples) / ((double) dt / 1000.);
			//		System.out.println(String.format("[DBG] [Dispatcher] task %3d %10d tuples %10.1f tuples/s queue size %d tstamp %13d", 
			//		num_tasks, ntuples, rate, (num_tasks + 1 - finished.get()), current_time));
			//	}
			//	previous_time = current_time;
			//	previous_tuplecount = current_tuplecount;
			//}
			num_tasks.incrementAndGet();
		}
	}
	
	public void taskFinished() {
		int finishedTmp = this.finished.incrementAndGet();
//		System.out.println("finished " + finishedTmp);
		if (finishedTmp == target)
			this.subQueryConnectable.getParentMultiOperator().targetReached();
	}
	
	public int getFinishedTasks() {
		return finished.get();
	}
	
	private int freshLogicalOrderID() {
		if (this.logicalOrderID == Integer.MAX_VALUE)
			this.logicalOrderID = 0;
		this.logicalOrderID++;
		return	this.logicalOrderID;
	}

	public int getNumberOfWindowsInBatch() {
		return SUB_QUERY_WINDOW_BATCH_COUNT;
	}
	
	public int getLogicalOrderID() {
		return this.logicalOrderID;
	}
	
	/*
	 * 
	 * BELOW IS THE OLD CODE:
	 * updating of windows is not done incrementally
	 * 
	 */
//	private void checkClearance() {
//
//		for (int s = 0; s < numberOfStreams; s++) {
//			if (waitForClearanceOfIndex[s] != -1) {
//				int indexToCheck = waitForClearanceOfIndex[s];
//				
//				/*
//				 * Clear if the respective buffer position is either empty
//				 * or filled with a new tuple that has a different timestamp
//				 */
//				if (this.buffer.get(indexToCheck) == null) {
//					waitForClearanceOfIndex[s] = -1;
//					clearanceLastTimestamp[s] = -1l;
//				}
//				else if (this.buffer.get(indexToCheck).timestamp != clearanceLastTimestamp[s]) {
//					waitForClearanceOfIndex[s] = -1;
//					clearanceLastTimestamp[s] = -1l;
//				}
//			}
//		}
//	}

	
//	public void dispatchTasks() {
//		
//		checkClearance();
//	
//		List<ISubQueryTaskCallable> tasks = new ArrayList<>();
//
//		// if we have data, create the tasks
//		while (sufficientDataForWindowBatch()) {
//			Map<Integer, IWindowBatch> windowBatches = new HashMap<>();
//			Map<SubQueryBuffer, Integer> freeUpToIndices = new HashMap<>();
//			for (Integer streamID : this.windowDefs.keySet()) {
//				IWindowDefinition windowDef = this.windowDefs.get(streamID); 
//				
//				long nextToProcessPointer = nextToProcessPointers.get(streamID);
//				switch (windowDef.getWindowType()) {
//				
//				case ROW_BASED:
//					/*
//					 *  define periodic window batch
//					 */
//					int start = (int) nextToProcessPointer;
//
//					int[] windowStartPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//					int[] windowEndPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//					
//					for (int w = 0; w < SUB_QUERY_WINDOW_BATCH_COUNT; w++) {
//						windowStartPointers[w] = w * (int)windowDef.getSlide();
//						windowEndPointers[w] = windowStartPointers[w] + (int)windowDef.getSize();
//					}
//					
//					IWindowBatch windowBatch = new BufferWindowBatch(buffer, windowStartPointers, windowEndPointers);
//					windowBatches.put(streamID, windowBatch);
//					
//					/*
//					 * update progress
//					 */
//					int followUpNextToProcessPointer = buffer.normIndex(start + (int)windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT);
//
//					/*
//					 * check whether we crossed the ring buffer boundary
//					 */
//					if (buffer.validIndex(followUpNextToProcessPointer))
//						if (!buffer.isMoreRecentThan(followUpNextToProcessPointer, (int)nextToProcessPointer)) {
//							waitForClearanceOfIndex.put(streamID, followUpNextToProcessPointer);
//							long t = buffer.get(followUpNextToProcessPointer).timestamp;
//							clearanceLastTimestamp.put(streamID, t);
//						}
//					
//					nextToProcessPointers.put(streamID, (long)followUpNextToProcessPointer);
//					
//					/*
//					 * Set indices that can be freed once the batch has been processed
//					 */
//					int freeUpToIndex = buffer.normIndex(start - 1 + (int)windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT);
//					if (!freeUpToIndices.containsKey(buffer))
//						freeUpToIndices.put(buffer, freeUpToIndex);
//					else 
//						if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), freeUpToIndex))
//							freeUpToIndices.put(buffer, freeUpToIndex);
//					break;
//	
//				case RANGE_BASED:
//					
//					long startTimeForWindowBatch = nextToProcessPointer;
//					long endTimeForWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
//					
//					
//					windowStartPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//					windowEndPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//					
//					int currentWindowStart = buffer.getStartIndex();
//					int currentWindowEnd;
//
//					for (int w = 0; w < SUB_QUERY_WINDOW_BATCH_COUNT; w++) {
//						
//						/*
//						 * Determine first index larger or equal than start timestamp for window
//						 */
//						// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
//						while (buffer.get(currentWindowStart).timestamp < startTimeForWindowBatch +  windowDef.getSlide() * w)
//							currentWindowStart++;
//						
//						windowStartPointers[w] = currentWindowStart;
//						
//						/*
//						 * Determine last index smaller or equal than end timestamp 
//						 * (note that currentWindowEnd is not normalized to the actual buffer index to ensure that it is larger than currentWindowStart)
//						 */
//						currentWindowEnd = currentWindowStart;
//						// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
//						while (buffer.get(currentWindowEnd).timestamp <= startTimeForWindowBatch +  windowDef.getSlide() * w + windowDef.getSize())
//							currentWindowEnd++;
//						
//						/*
//						 *  Check whether the window is actually empty
//						 */
//						if (currentWindowStart == currentWindowEnd) {
//							// Signal empty window by setting the indices to -1
//							windowStartPointers[w] = -1;
//							windowEndPointers[w] = -1;
//						}
//						else {
//							// Make sure to store the last one inside the window
//							windowEndPointers[w] = currentWindowEnd - 1;
//						}
//					}
//
//					/*
//					 *  Determine last index smaller or equal than the start for the next window batch 
//					 */
//					long timestampForNextWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT;
//					int indexBeforeNextWindowBatch = windowStartPointers[windowStartPointers.length - 1];
//					while (buffer.get(indexBeforeNextWindowBatch).timestamp < timestampForNextWindowBatch)
//						indexBeforeNextWindowBatch++;
//					
//					indexBeforeNextWindowBatch = buffer.getIndexBefore(indexBeforeNextWindowBatch, 1);
//
//					// define periodic window batch
//					//System.out.println("RANGE BATCH:\t buffer view:\t" + windowStartPointers[0] + "-" + windowEndPointers[windowEndPointers.length - 1]+ "\t time:\t" +  startTimeForWindowBatch + "-" +  endTimeForWindowBatch);
//					windowBatch = new BufferWindowBatch(buffer, windowStartPointers, windowEndPointers, startTimeForWindowBatch, endTimeForWindowBatch);
//					windowBatches.put(streamID, windowBatch);
//
//					// update progress
//					nextToProcessPointers.put(streamID, timestampForNextWindowBatch);
//
//					if (!freeUpToIndices.containsKey(buffer))
//						freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);
//					else 
//						if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), indexBeforeNextWindowBatch))
//							freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);
//
//					break;
//	
//				default:
//					LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
//					break;
//				}
//			}
//			ISubQueryTaskCallable task;
//			if (GPU)
//				task = new SubQueryTaskGPUCallable(windowBatches, freshLogicalOrderID(), freeUpToIndices, gpu);
//			else
//				task = new SubQueryTaskCPUCallable(subQueryConnectable, windowBatches, freshLogicalOrderID(), freeUpToIndices);
//			tasks.add(task);
//		}
//				
//	}	
//	private boolean sufficientDataForWindowBatch() {
//
//		boolean sufficientData = true;
//		for (Integer streamID : subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
//			sufficientData &= sufficientDataForStream(streamID, subQueryConnectable.getLocalUpstreamBuffers().get(streamID));
//			if (!sufficientData)
//				break;
//		}
//		
//		return sufficientData;
//	}

//	private boolean sufficientDataForStream(Integer streamID, IWindowDefinition windowDef){
//
//		/*
//		 * If the buffer is empty, return false
//		 */
//		if (this.buffer.size() == 0)
//			return false;
//
//		/*
//		 * Note that nextToProcessPointer may refer to an index in the 
//		 * buffer (row based window) or a timestamp (range based window)
//		 */
//		boolean sufficientData = true;
//		long nextToProcessPointer = nextToProcessPointers.get(streamID);
//		switch (windowDef.getWindowType()) {
//		case ROW_BASED:
//			// if we have not yet processed any tuple, we take the first in the buffer
//			if (nextToProcessPointer == -1) {
//				nextToProcessPointer = buffer.getStartIndex();
//				nextToProcessPointers.put(streamID, nextToProcessPointer);
//			}
//			
//			if (waitForClearanceOfIndex.containsKey(streamID)){
//				sufficientData = false;
//				break;
//			}
//				
//			// pointing into an area that is not yet filled?
//			if (!buffer.validIndex((int) nextToProcessPointer)){
//				sufficientData = false;
//				break;
//			}
//
//			int unprocessedTuples = (buffer.getStartIndex() == (int)nextToProcessPointer)?
//					buffer.size() :
//					(buffer.getEndIndex() + buffer.capacity() - (int) nextToProcessPointer) % buffer.capacity();
//			// is that enough data given the window definition?
//			sufficientData &= (unprocessedTuples >= (int)windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + (int)windowDef.getSize());
//			break;
//		case RANGE_BASED:
//			// if we have not yet processed any tuple, we take the first in the buffer
//			if (nextToProcessPointer == -1) {
//				nextToProcessPointer = buffer.get(buffer.getStartIndex()).timestamp;
//				nextToProcessPointers.put(streamID, nextToProcessPointer);
//			}
//			long endTimeForWindowBatch = nextToProcessPointer + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
//			// check whether end time for window batch has passed already
////			sufficientData &= (endTimeForWindowBatch < buffer.getMostRecent().timestamp);
//			sufficientData &= (endTimeForWindowBatch < buffer.get(buffer.getIndexBefore(buffer.getEndIndex(),1)).timestamp);
//			break;
//
//		default:
//			LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
//			break;
//		}
//		return sufficientData;
//	}

	

}
