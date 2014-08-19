package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.gpu.GPUExecutionContext;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryTaskCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCPUCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskGPUCallable;
import uk.ac.imperial.lsds.seep.operator.compose.window.BufferWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQueryBufferWrapper {
	
	private final Logger LOG = LoggerFactory.getLogger(SubQueryBufferWrapper.class);

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	private SubQueryBuffer buffer;
	private Map<Integer, IWindowDefinition> windowDefs;
	private MultiOperator multiOp;
	
	/*
	 * Counter for dispatched tasks 
	 */
	private int logicalOrderID = -1;
	
	private boolean GPU;
	private GPUExecutionContext gpu;

	/*
	 * Internal data structures
	 */
	private int[] originalStreamIDs;
	int numberOfStreams;

	private int[] currentElementIndexStart;
	private int[] currentElementIndexEnd;
//	private int[] waitForClearanceOfIndex;
//	private long[] clearanceLastTimestamp;
	
//	private Map<Integer, Long> nextToProcessPointers;
//	private Map<Integer, Integer> waitForClearanceOfIndex;
//	private Map<Integer, Long> clearanceLastTimestamp;

	private int[] currentWindowStartPointer;
	private int[] currentWindowEndPointer;

//	private int[] currentBatchOffsetStartPointer;
//	private int[] currentBatchOffsetEndPointer;

	private Map<Integer, Deque<IWindowBatch>> windowBatches;
	private Map<Integer, Deque<IWindowBatch>> windowBatchesEnd;
	private Map<Integer, Deque<IWindowBatch>> windowBatchesNextForEnd;
	
	private Map<IWindowBatch, Integer> freeUpTo;
	
	
	public SubQueryBufferWrapper(Map<Integer, IWindowDefinition> windowDefs, MultiOperator multiOp) {

		assert(SUB_QUERY_WINDOW_BATCH_COUNT > 0);

		this.windowDefs = windowDefs;
		this.multiOp = multiOp;
		
		this.originalStreamIDs = new int[this.windowDefs.keySet().size()];
		numberOfStreams = 0;
		for (Integer streamID : this.windowDefs.keySet()) 
			this.originalStreamIDs[numberOfStreams++] = streamID;
		
		numberOfStreams--;
		
		this.currentElementIndexStart = new int[numberOfStreams];
		this.currentElementIndexEnd = new int[numberOfStreams];
		
		for (int s = 0; s < numberOfStreams; s++) {
			windowBatches.put(s, new LinkedList<IWindowBatch>());
			windowBatchesEnd.put(s, new LinkedList<IWindowBatch>());
			windowBatchesNextForEnd.put(s, new LinkedList<IWindowBatch>());
			IWindowBatch windowBatch = new BufferWindowBatch(buffer, new int[SUB_QUERY_WINDOW_BATCH_COUNT],  new int[SUB_QUERY_WINDOW_BATCH_COUNT]);
			windowBatches.get(s).add(windowBatch);
			windowBatchesEnd.get(s).add(windowBatch);
		}
		
//		this.startPointersBatch = new int[numberOfStreams][SUB_QUERY_WINDOW_BATCH_COUNT];
//		this.endPointersBatch = new int[numberOfStreams][SUB_QUERY_WINDOW_BATCH_COUNT];
//		Arrays.fill(this.startPointersBatch, -1);
//		Arrays.fill(this.endPointersBatch, -1);
		
		this.currentWindowStartPointer = new int[numberOfStreams];
		this.currentWindowEndPointer = new int[numberOfStreams];
		Arrays.fill(this.currentWindowStartPointer, -1);
		Arrays.fill(this.currentWindowEndPointer, -1);

//		this.currentBatchOffsetStartPointer = new int[numberOfStreams];
//		this.currentBatchOffsetEndPointer = new int[numberOfStreams];
//		Arrays.fill(this.currentBatchOffsetStartPointer, 0);
//		Arrays.fill(this.currentBatchOffsetEndPointer, 0);

		GPU = this.multiOp.isGPUEnabled();
		gpu = this.multiOp.getGPUContext();
		
		freeUpTo = new HashMap<>();
		
	}
	
	public SubQueryBuffer getBuffer() {
		return this.buffer;
	}
	
	/*
	 * ###############################################
	 * Methods that hook into the update routines
	 * for the sub query buffer
	 * ###############################################
	 */
	public MultiOpTuple[] add(MultiOpTuple[] tuples) {
		MultiOpTuple[] result = this.buffer.add(tuples);
		for (int i = 0; i < tuples.length - result.length; i++)
			updateCurrentWindows(tuples[i]);
		
		return result;
	}
	
	public boolean add(MultiOpTuple element) {
		boolean result = this.buffer.add(element);
		if (result)
			updateCurrentWindows(element);
		return result;
	}
	
	private void updateCurrentWindows(MultiOpTuple element) {
		
		for (int s = 0; s < numberOfStreams; s++) {
			IWindowDefinition windowDef = this.windowDefs.get(originalStreamIDs[s]); 
			IWindowBatch windowBatchStart = windowBatches.get(s).getLast();
			IWindowBatch windowBatchEnd = windowBatchesEnd.get(s).getLast();
			
			switch (windowDef.getWindowType()) {
			
			case ROW_BASED:

				/* 
				 * Update current element index. Note that we use to indices
				 * in order to ensure that they can be normalised when crossing
				 * the boundary of a window batch, even though this may happen 
				 * independently for the start and end pointers
				 */
				if (this.currentWindowStartPointer[s] == -1) {
					this.currentWindowStartPointer[s] = 0;
					this.currentElementIndexStart[s] = this.buffer.getStartIndex();
					this.currentElementIndexEnd[s] = this.buffer.getStartIndex();
					windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer[s]] = this.currentElementIndexStart[s];
				}
				else {
					this.currentElementIndexStart[s]++;
					this.currentElementIndexEnd[s]++;
				}

				/*
				 * Should we open a new window?
				 */
				if (windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer[s]] + (int) windowDef.getSlide() >=  this.currentElementIndexStart[s]) {
					this.currentWindowStartPointer[s]++;
					
					// Is the new window part of the next window batch?
					if (this.currentWindowStartPointer[s] >= SUB_QUERY_WINDOW_BATCH_COUNT) {
						// Store free indices for current batch
						freeUpTo.put(windowBatchStart, this.currentElementIndexStart[s]-1);
						// Create new window batch
						windowBatchStart = new BufferWindowBatch(buffer, new int[SUB_QUERY_WINDOW_BATCH_COUNT],  new int[SUB_QUERY_WINDOW_BATCH_COUNT]);
						this.windowBatches.get(s).add(windowBatchStart);
						this.windowBatchesNextForEnd.get(s).add(windowBatchStart);
						// Set the current window pointer to zero, i.e., the first window of the new batch
						this.currentWindowStartPointer[s] = 0;
						
						// In the new window batch, we can use normalized indices again
						this.currentElementIndexStart[s] = buffer.normIndex(this.currentElementIndexStart[s]);
					}
					windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer[s]] = this.currentElementIndexStart[s];
				}

				/*
				 * Should we close the oldest open window?
				 */
				boolean toClose = false;
				
				if (this.currentWindowEndPointer[s] == -1) {
					/*
					 * We have not yet closed any window, so we need to do the check based on the first opened window
					 */
					toClose |= (windowBatches.get(s).getFirst().getWindowStartPointers()[0] + (int) windowDef.getSize() >=  this.currentElementIndexEnd[s]);
				}
				else {
					toClose |= (windowBatchEnd.getWindowEndPointers()[this.currentWindowEndPointer[s]] + (int) windowDef.getSlide() >=  this.currentElementIndexEnd[s]);
				}
				
				if (toClose) {
					this.currentWindowEndPointer[s]++;
					
					// Will the next window to close be part of the next window batch?
					if (this.currentWindowEndPointer[s] >= SUB_QUERY_WINDOW_BATCH_COUNT) {
						// Move to new window batch
						windowBatchEnd = windowBatchesNextForEnd.get(s).poll();
						windowBatchesEnd.get(s).add(windowBatchEnd);
						// Set the current window pointer to zero, i.e., the first window of the new batch
						this.currentWindowEndPointer[s] = 0;
						
						// In the new window batch, we can use normalized indices again
						this.currentElementIndexEnd[s] = buffer.normIndex(this.currentElementIndexEnd[s]);
					}
					windowBatchEnd.getWindowEndPointers()[this.currentWindowEndPointer[s]] = this.currentElementIndexEnd[s];
				}
				
				break;

			case RANGE_BASED:
				
//				long startTimeForWindowBatch = nextToProcessPointer;
//				long endTimeForWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
//				
//				
//				windowStartPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//				windowEndPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
//				
//				int currentWindowStart = buffer.getStartIndex();
//				int currentWindowEnd;
//
//				for (int w = 0; w < SUB_QUERY_WINDOW_BATCH_COUNT; w++) {
//					
//					/*
//					 * Determine first index larger or equal than start timestamp for window
//					 */
//					// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
//					while (buffer.get(currentWindowStart).timestamp < startTimeForWindowBatch +  windowDef.getSlide() * w)
//						currentWindowStart++;
//					
//					windowStartPointers[w] = currentWindowStart;
//					
//					/*
//					 * Determine last index smaller or equal than end timestamp 
//					 * (note that currentWindowEnd is not normalized to the actual buffer index to ensure that it is larger than currentWindowStart)
//					 */
//					currentWindowEnd = currentWindowStart;
//					// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
//					while (buffer.get(currentWindowEnd).timestamp <= startTimeForWindowBatch +  windowDef.getSlide() * w + windowDef.getSize())
//						currentWindowEnd++;
//					
//					/*
//					 *  Check whether the window is actually empty
//					 */
//					if (currentWindowStart == currentWindowEnd) {
//						// Signal empty window by setting the indices to -1
//						windowStartPointers[w] = -1;
//						windowEndPointers[w] = -1;
//					}
//					else {
//						// Make sure to store the last one inside the window
//						windowEndPointers[w] = currentWindowEnd - 1;
//					}
//				}
//
//				/*
//				 *  Determine last index smaller or equal than the start for the next window batch 
//				 */
//				long timestampForNextWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT;
//				int indexBeforeNextWindowBatch = windowStartPointers[windowStartPointers.length - 1];
//				while (buffer.get(indexBeforeNextWindowBatch).timestamp < timestampForNextWindowBatch)
//					indexBeforeNextWindowBatch++;
//				
//				indexBeforeNextWindowBatch = buffer.getIndexBefore(indexBeforeNextWindowBatch, 1);
//
//				// define periodic window batch
//				//System.out.println("RANGE BATCH:\t buffer view:\t" + windowStartPointers[0] + "-" + windowEndPointers[windowEndPointers.length - 1]+ "\t time:\t" +  startTimeForWindowBatch + "-" +  endTimeForWindowBatch);
//				windowBatch = new BufferWindowBatch(buffer, windowStartPointers, windowEndPointers, startTimeForWindowBatch, endTimeForWindowBatch);
//				windowBatches.put(streamID, windowBatch);
//
//				// update progress
//				nextToProcessPointers.put(streamID, timestampForNextWindowBatch);
//
//				if (!freeUpToIndices.containsKey(buffer))
//					freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);
//				else 
//					if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), indexBeforeNextWindowBatch))
//						freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);

				break;

			default:
				LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
				break;
			}
		}
	}
	
	public void dispatchTasks() {
		
	}

	
	
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
	
	private int freshLogicalOrderID() {
		if (this.logicalOrderID == Integer.MAX_VALUE)
			this.logicalOrderID = 0;
		this.logicalOrderID++;
		return	this.logicalOrderID;
	}
	
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
