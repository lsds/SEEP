package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.BufferWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class WindowBatchTaskCreationScheme implements
		SubQueryTaskCreationScheme {

	private final Logger LOG = LoggerFactory.getLogger(WindowBatchTaskCreationScheme.class);

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	private int logicalOrderID = -1;

	private ISubQueryConnectable subQueryConnectable;

	private Map<Integer, Long> nextToProcessPointers;
	private Map<Integer, Integer> waitForClearanceOfIndex;
	private Map<Integer, Long> clearanceLastTimestamp;
	
	public WindowBatchTaskCreationScheme(ISubQueryConnectable subQueryConnectable){
		this.subQueryConnectable = subQueryConnectable;
		this.nextToProcessPointers = new HashMap<>();
		this.waitForClearanceOfIndex = new HashMap<>();
		this.clearanceLastTimestamp = new HashMap<>();
		for (Integer streamID : this.subQueryConnectable.getLocalUpstreamBuffers().keySet()) 
			this.nextToProcessPointers.put(streamID, -1l);
	}
	
	private void checkClearance() {

		for (Integer streamID : subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
			if (waitForClearanceOfIndex.containsKey(streamID)) {
				int indexToCheck = waitForClearanceOfIndex.get(streamID);
				
				/*
				 * Clear if the respective buffer position is either empty
				 * or filled with a new tuple that has a different timestamp
				 */
				if (subQueryConnectable.getLocalUpstreamBuffers().get(streamID).get(indexToCheck) == null) {
					waitForClearanceOfIndex.remove(streamID);
					clearanceLastTimestamp.remove(streamID);
				}
				else if (subQueryConnectable.getLocalUpstreamBuffers().get(streamID).get(indexToCheck).timestamp != clearanceLastTimestamp.get(streamID)) {
					waitForClearanceOfIndex.remove(streamID);
					clearanceLastTimestamp.remove(streamID);
				}
			}
		}
	}
	
	@Override
	public List<ISubQueryTaskCallable> createTasks() {
		
		checkClearance();
		
		Map<Integer, IWindowDefinition> winDefs = subQueryConnectable.getSubQuery().getWindowDefinitions();
		
		assert(SUB_QUERY_WINDOW_BATCH_COUNT > 0);
		
		List<ISubQueryTaskCallable> tasks = new ArrayList<>();

		// if we have data, create the tasks
		while (sufficientDataForWindowBatch()) {
			Map<Integer, IWindowBatch> windowBatches = new HashMap<>();
			Map<SubQueryBuffer, Integer> freeUpToIndices = new HashMap<>();
			for (Integer streamID : subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
				
				SubQueryBuffer buffer = subQueryConnectable.getLocalUpstreamBuffers().get(streamID);
	
				IWindowDefinition windowDef = winDefs.get(streamID); 
				long nextToProcessPointer = nextToProcessPointers.get(streamID);
				switch (windowDef.getWindowType()) {
				
				case ROW_BASED:
					/*
					 *  define periodic window batch
					 */
					int start = (int) nextToProcessPointer;

					int[] windowStartPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
					int[] windowEndPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
					
					for (int w = 0; w < SUB_QUERY_WINDOW_BATCH_COUNT; w++) {
						windowStartPointers[w] = w * (int)windowDef.getSlide();
						windowEndPointers[w] = windowStartPointers[w] + (int)windowDef.getSize();
					}
					
					IWindowBatch windowBatch = new BufferWindowBatch(buffer, windowStartPointers, windowEndPointers);
					windowBatches.put(streamID, windowBatch);
					
					/*
					 * update progress
					 */
					int followUpNextToProcessPointer = buffer.normIndex(start + (int)windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT);

					/*
					 * check whether we crossed the ring buffer boundary
					 */
					if (buffer.validIndex(followUpNextToProcessPointer))
						if (!buffer.isMoreRecentThan(followUpNextToProcessPointer, (int)nextToProcessPointer)) {
							waitForClearanceOfIndex.put(streamID, followUpNextToProcessPointer);
							long t = buffer.get(followUpNextToProcessPointer).timestamp;
							clearanceLastTimestamp.put(streamID, t);
						}
					
					nextToProcessPointers.put(streamID, (long)followUpNextToProcessPointer);
					
					/*
					 * Set indices that can be freed once the batch has been processed
					 */
					int freeUpToIndex = buffer.normIndex(start - 1 + (int)windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT);
					if (!freeUpToIndices.containsKey(buffer))
						freeUpToIndices.put(buffer, freeUpToIndex);
					else 
						if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), freeUpToIndex))
							freeUpToIndices.put(buffer, freeUpToIndex);
					break;
	
				case RANGE_BASED:
					
					long startTimeForWindowBatch = nextToProcessPointer;
					long endTimeForWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
					
					
					windowStartPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
					windowEndPointers = new int[SUB_QUERY_WINDOW_BATCH_COUNT];
					
					int currentWindowStart = buffer.getStartIndex();
					int currentWindowEnd;

					for (int w = 0; w < SUB_QUERY_WINDOW_BATCH_COUNT; w++) {
						
						/*
						 * Determine first index larger or equal than start timestamp for window
						 */
						// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
						while (buffer.get(currentWindowStart).timestamp < startTimeForWindowBatch +  windowDef.getSlide() * w)
							currentWindowStart++;
						
						windowStartPointers[w] = currentWindowStart;
						
						/*
						 * Determine last index smaller or equal than end timestamp 
						 * (note that currentWindowEnd is not normalized to the actual buffer index to ensure that it is larger than currentWindowStart)
						 */
						currentWindowEnd = currentWindowStart;
						// The following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
						while (buffer.get(currentWindowEnd).timestamp <= startTimeForWindowBatch +  windowDef.getSlide() * w + windowDef.getSize())
							currentWindowEnd++;
						
						/*
						 *  Check whether the window is actually empty
						 */
						if (currentWindowStart == currentWindowEnd) {
							// Signal empty window by setting the indices to -1
							windowStartPointers[w] = -1;
							windowEndPointers[w] = -1;
						}
						else {
							// Make sure to store the last one inside the window
							windowEndPointers[w] = currentWindowEnd - 1;
						}
					}

					/*
					 *  Determine last index smaller or equal than the start for the next window batch 
					 */
					long timestampForNextWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT;
					int indexBeforeNextWindowBatch = windowStartPointers[windowStartPointers.length - 1];
					while (buffer.get(indexBeforeNextWindowBatch).timestamp < timestampForNextWindowBatch)
						indexBeforeNextWindowBatch++;
					
					indexBeforeNextWindowBatch = buffer.getIndexBefore(indexBeforeNextWindowBatch, 1);

					// define periodic window batch
					//System.out.println("RANGE BATCH:\t buffer view:\t" + windowStartPointers[0] + "-" + windowEndPointers[windowEndPointers.length - 1]+ "\t time:\t" +  startTimeForWindowBatch + "-" +  endTimeForWindowBatch);
					windowBatch = new BufferWindowBatch(buffer, windowStartPointers, windowEndPointers, startTimeForWindowBatch, endTimeForWindowBatch);
					windowBatches.put(streamID, windowBatch);

					// update progress
					nextToProcessPointers.put(streamID, timestampForNextWindowBatch);

					if (!freeUpToIndices.containsKey(buffer))
						freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);
					else 
						if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), indexBeforeNextWindowBatch))
							freeUpToIndices.put(buffer, indexBeforeNextWindowBatch);

					break;
	
				default:
					LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
					break;
				}
			}
			ISubQueryTaskCallable task = new SubQueryTaskCPUCallable(subQueryConnectable, windowBatches, freshLogicalOrderID(), freeUpToIndices);
			tasks.add(task);
		}
				
		return tasks;
	}
	
	private int freshLogicalOrderID() {
		if (this.logicalOrderID == Integer.MAX_VALUE)
			this.logicalOrderID = 0;
		this.logicalOrderID++;
		return	this.logicalOrderID;
	}
	
	private boolean sufficientDataForWindowBatch() {

		boolean sufficientData = true;
		for (Integer streamID : subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
			sufficientData &= sufficientDataForStream(streamID, subQueryConnectable.getLocalUpstreamBuffers().get(streamID));
			if (!sufficientData)
				break;
		}
		
		return sufficientData;
	}


	private boolean sufficientDataForStream(Integer streamID, SubQueryBuffer buffer){

		/*
		 * If the buffer is empty, return false
		 */
		if (buffer.size() == 0)
			return false;

		/*
		 * Note that nextToProcessPointer may refer to an index in the 
		 * buffer (row based window) or a timestamp (range based window)
		 */
		boolean sufficientData = true;
		IWindowDefinition windowDef = subQueryConnectable.getSubQuery().getWindowDefinitions().get(streamID); 
		long nextToProcessPointer = nextToProcessPointers.get(streamID);
		switch (windowDef.getWindowType()) {
		case ROW_BASED:
			// if we have not yet processed any tuple, we take the first in the buffer
			if (nextToProcessPointer == -1) {
				nextToProcessPointer = buffer.getStartIndex();
				nextToProcessPointers.put(streamID, nextToProcessPointer);
			}
			
			if (waitForClearanceOfIndex.containsKey(streamID)){
				sufficientData = false;
				break;
			}
				
			// pointing into an area that is not yet filled?
			if (!buffer.validIndex((int) nextToProcessPointer)){
				sufficientData = false;
				break;
			}

			int unprocessedTuples = (buffer.getStartIndex() == (int)nextToProcessPointer)?
					buffer.size() :
					(buffer.getEndIndex() + buffer.capacity() - (int) nextToProcessPointer) % buffer.capacity();
			// is that enough data given the window definition?
			sufficientData &= (unprocessedTuples >= (int)windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + (int)windowDef.getSize());
			break;
		case RANGE_BASED:
			// if we have not yet processed any tuple, we take the first in the buffer
			if (nextToProcessPointer == -1) {
				nextToProcessPointer = buffer.get(buffer.getStartIndex()).timestamp;
				nextToProcessPointers.put(streamID, nextToProcessPointer);
			}
			long endTimeForWindowBatch = nextToProcessPointer + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
			// check whether end time for window batch has passed already
//			sufficientData &= (endTimeForWindowBatch < buffer.getMostRecent().timestamp);
			sufficientData &= (endTimeForWindowBatch < buffer.get(buffer.getIndexBefore(buffer.getEndIndex(),1)).timestamp);
			break;

		default:
			LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
			break;
		}
		return sufficientData;
	}

	
}
