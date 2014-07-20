package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.IPeriodicWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.PeriodicWindowBatch;

public class WindowBatchTaskCreationScheme implements
		SubQueryTaskCreationScheme {

	private final Logger LOG = LoggerFactory.getLogger(WindowBatchTaskCreationScheme.class);

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	private Iterator<SubQueryTaskCallable> iter;
	
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
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public SubQueryTaskCallable next() {
		return iter.next();
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException("");
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
				else if (subQueryConnectable.getLocalUpstreamBuffers().get(streamID).get(indexToCheck).getPayload().timestamp != clearanceLastTimestamp.get(streamID)) {
					waitForClearanceOfIndex.remove(streamID);
					clearanceLastTimestamp.remove(streamID);
				}
			}
		}
	}
	
	@Override
	public void createTasks() {
		
		checkClearance();
		
		Map<Integer, IWindowDefinition> winDefs = subQueryConnectable.getSubQuery().getWindowDefinitions();
		
		assert(SUB_QUERY_WINDOW_BATCH_COUNT > 0);
		
		List<SubQueryTaskCallable> tasks = new ArrayList<SubQueryTaskCallable>();

		// if we have data, create the tasks
		boolean sufficientData = sufficientDataForWindowBatch();
		while (sufficientData) {
			Map<Integer, IWindowBatch> windowBatches = new HashMap<>();
			Map<SubQueryBuffer, Integer> freeUpToIndices = new HashMap<>();
			for (Integer streamID : subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
				
				SubQueryBuffer buffer = subQueryConnectable.getLocalUpstreamBuffers().get(streamID);
	
				IWindowDefinition windowDef = winDefs.get(streamID); 
				long nextToProcessPointer = nextToProcessPointers.get(streamID);
				switch (windowDef.getWindowType()) {
				
				case ROW_BASED:
					// define periodic window batch
					int start = (int) nextToProcessPointer;
					int end = start + (int) windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + (int)windowDef.getSize();
					IPeriodicWindowBatch windowBatch = new PeriodicWindowBatch(windowDef, buffer, start, end);
					windowBatches.put(streamID, windowBatch);
					// update progress
					int followUpNextToProcessPointer = buffer.normIndex(start + (int)windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT);
					// check whether we crossed the ring buffer boundary
					if (buffer.validIndex(followUpNextToProcessPointer))
						if (!buffer.isMoreRecentThan(followUpNextToProcessPointer, (int)nextToProcessPointer)) {
							waitForClearanceOfIndex.put(streamID, followUpNextToProcessPointer);
							long t = buffer.get(followUpNextToProcessPointer).getPayload().timestamp;
							clearanceLastTimestamp.put(streamID, t);
						}
					
					nextToProcessPointers.put(streamID, (long)followUpNextToProcessPointer);
					
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
					// determine first index larger or equal than start timestamp
					start = buffer.getStartIndex();
					// the following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
					while (buffer.get(start).getPayload().timestamp < startTimeForWindowBatch)
						start = buffer.normIndex(start + 1);
					
					// determine last index smaller or equal than end timestamp (note that "end" does not store the index to ensure that it is large than the start)
					end = start;
					// the following loop terminates since we checked that there is a tuple with timestamp larger than the end time of the window batch in the buffer
					while (buffer.get(buffer.normIndex(end)).getPayload().timestamp <= endTimeForWindowBatch)
						end++;

					// determine last index smaller or equal than the start for the next window batch 
					long timestampForNextWindowBatch = startTimeForWindowBatch + windowDef.getSlide() * SUB_QUERY_WINDOW_BATCH_COUNT;
					int indexBeforNextWindowBatch = start;
					while (buffer.get(indexBeforNextWindowBatch).getPayload().timestamp < timestampForNextWindowBatch)
						indexBeforNextWindowBatch = buffer.normIndex(indexBeforNextWindowBatch + 1);

					// define periodic window batch
					windowBatch = new PeriodicWindowBatch(windowDef, buffer, start, end, startTimeForWindowBatch, endTimeForWindowBatch);
					windowBatches.put(streamID, windowBatch);

					// update progress
					nextToProcessPointers.put(streamID, timestampForNextWindowBatch);

					if (!freeUpToIndices.containsKey(buffer))
						freeUpToIndices.put(buffer, indexBeforNextWindowBatch);
					else 
						if (buffer.isMoreRecentThan(freeUpToIndices.get(buffer), indexBeforNextWindowBatch))
							freeUpToIndices.put(buffer, indexBeforNextWindowBatch);

					break;
	
				default:
					LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
					break;
				}
			}
			SubQueryTaskCallable task = new SubQueryTaskCallable(subQueryConnectable, windowBatches, freshLogicalOrderID(), freeUpToIndices);
			tasks.add(task);
			sufficientData = sufficientDataForWindowBatch();
		}
				
		this.iter = tasks.iterator();
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
				if (buffer.size() == 0) {
					sufficientData = false;
					break;
				}
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
				if (buffer.size() == 0) {
					sufficientData = false;
					break;
				}
				nextToProcessPointer = buffer.get(buffer.getStartIndex()).getPayload().timestamp;
				nextToProcessPointers.put(streamID, nextToProcessPointer);
			}
			long endTimeForWindowBatch = nextToProcessPointer + windowDef.getSlide() * (SUB_QUERY_WINDOW_BATCH_COUNT-1) + windowDef.getSize();
			// check whether end time for window batch has passed already
			sufficientData &= (endTimeForWindowBatch < buffer.get(buffer.getEndIndex()).getPayload().timestamp);
			break;

		default:
			LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
			break;
		}
		return sufficientData;
	}

	
}
