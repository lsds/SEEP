package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.BufferWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQueryBufferWindowWrapper {
	
	private final Logger LOG = LoggerFactory.getLogger(SubQueryBufferWindowWrapper.class);

	/*
	 * Configuration of wrapper
	 */
	private ISubQueryConnectable connectable;
	private SubQueryBuffer buffer;
	private IWindowDefinition windowDef;
	private int numberOfWindowsInBatch;
	
	
	/*
	 * Data structures for window state
	 */
	private int currentElementIndexStart;
	private int currentElementIndexEnd;

	private int currentWindowStartPointer;
	private int currentWindowEndPointer;

	private Deque<IWindowBatch> windowBatches;
	private Deque<IWindowBatch> windowBatchesEnd;
	private Deque<IWindowBatch> windowBatchesNextForEnd;
		
	private Map<IWindowBatch, Integer> freeUpTo;

	public SubQueryBufferWindowWrapper(ISubQueryConnectable connectable, int streamID) {

		this.buffer = new SubQueryBuffer();
		this.windowDef = connectable.getWindowDefinitions().get(streamID);
		this.connectable = connectable;
		this.numberOfWindowsInBatch = this.connectable.getTaskDispatcher().getNumberOfWindowsInBatch();
		
		this.currentElementIndexStart = -1;
		this.currentElementIndexEnd = -1;
		
		windowBatches = new LinkedList<IWindowBatch>();
		windowBatchesEnd = new LinkedList<IWindowBatch>();
		windowBatchesNextForEnd = new LinkedList<IWindowBatch>();

		IWindowBatch windowBatch = new BufferWindowBatch(this.buffer, freshInitializedArray(this.numberOfWindowsInBatch,-1),  freshInitializedArray(this.numberOfWindowsInBatch,-1));
		windowBatches.add(windowBatch);
		windowBatchesEnd.add(windowBatch);
		
		this.currentWindowStartPointer = -1;
		this.currentWindowEndPointer = -1;
		this.freeUpTo = new HashMap<>();
	}
	
	/**
	 * Hook into the adding routine for the buffer to 
	 * make sure that, if an element is added to the buffer
	 * the wrapper updates the window state accordingly
	 * 
	 * @param element to be added to the buffer
	 * @return true if the element has been added, false otherwise
	 */
	public boolean addToBuffer(MultiOpTuple element) {
		boolean added = this.buffer.add(element);
		if (added)
			updateCurrentWindow(element);
		return added;
	}
	
	public Object getExternalBufferLock() {
		return this.buffer.getExternalLock();
	}
	
	public void freeUpToIndexInBuffer(int i) {
		this.buffer.freeUpToIndex(i);
	}
	
	private void updateCurrentWindow(MultiOpTuple element) {

		IWindowBatch windowBatchStart = windowBatches.getLast();
		IWindowBatch windowBatchEnd = windowBatchesEnd.getLast();
		
		switch (windowDef.getWindowType()) {
				
		case ROW_BASED:

			/* 
			 * Update current element index. Note that we use to indices
			 * in order to ensure that they can be normalised when crossing
			 * the boundary of a window batch, even though this may happen 
			 * independently for the start and end pointers
			 */
			if (this.currentWindowStartPointer == -1) {
				this.currentWindowStartPointer = 0;
				this.currentElementIndexStart = buffer.getStartIndex();
				this.currentElementIndexEnd = buffer.getStartIndex();
				windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer] = this.currentElementIndexStart;
				windowBatchStart.setStartTimestamp(buffer.get(buffer.getStartIndex()).timestamp);
			}
			else {
				this.currentElementIndexStart++;
				this.currentElementIndexEnd++;
			}

			/*
			 * Should we open a new window?
			 */
			if (this.currentElementIndexStart >= windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer] + (int) windowDef.getSlide()) {
				this.currentWindowStartPointer++;
				
				// Is the new window part of the next window batch?
				if (this.currentWindowStartPointer >= this.numberOfWindowsInBatch) {
					// Store free indices for current batch
					freeUpTo.put(windowBatchStart, this.currentElementIndexStart-1);
					// Create new window batch
					windowBatchStart = new BufferWindowBatch(buffer, new int[this.numberOfWindowsInBatch],  new int[this.numberOfWindowsInBatch]);
					this.windowBatches.add(windowBatchStart);
					this.windowBatchesNextForEnd.add(windowBatchStart);
					// Set the current window pointer to zero, i.e., the first window of the new batch
					this.currentWindowStartPointer = 0;
					
					// In the new window batch, we can use normalized indices again
					this.currentElementIndexStart = buffer.normIndex(this.currentElementIndexStart);
					// Set start time for the new window batch
					windowBatchStart.setStartTimestamp(buffer.get(this.currentElementIndexStart).timestamp);
				}
				windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer] = this.currentElementIndexStart;
			}

			/*
			 * Should we close the oldest open window?
			 */
			boolean toClose = false;
			
			if (this.currentWindowEndPointer == -1) {
				/*
				 * We have not yet closed any window, so we need to do the check based on the first opened window
				 */
				toClose |= (this.currentElementIndexEnd >= windowBatches.getFirst().getWindowStartPointers()[0] + (int) windowDef.getSize());
			}
			else {
				toClose |= (this.currentElementIndexEnd >= windowBatchEnd.getWindowEndPointers()[this.currentWindowEndPointer] + (int) windowDef.getSlide());
			}
			
			if (toClose) {
				this.currentWindowEndPointer++;
				
				// Will the next window to close be part of the next window batch?
				if (this.currentWindowEndPointer >= this.numberOfWindowsInBatch) {
					// Set end time for the old window batch
					windowBatchEnd.setEndTimestamp(buffer.get(this.currentElementIndexEnd-1).timestamp);
					
					// Move to new window batch
					windowBatchEnd = windowBatchesNextForEnd.poll();
					windowBatchesEnd.add(windowBatchEnd);
					// Set the current window pointer to zero, i.e., the first window of the new batch
					this.currentWindowEndPointer = 0;
					
					// In the new window batch, we can use normalized indices again
					this.currentElementIndexEnd = buffer.normIndex(this.currentElementIndexEnd);
				}
				windowBatchEnd.getWindowEndPointers()[this.currentWindowEndPointer] = this.currentElementIndexEnd;
			}
			
			break;
	
		case RANGE_BASED:
			
			if (this.currentWindowStartPointer == -1) {
				this.currentWindowStartPointer = 0;
				this.currentElementIndexStart = buffer.getStartIndex();
				this.currentElementIndexEnd = buffer.getStartIndex();
				windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer] = this.currentElementIndexStart;
				long startTime = buffer.get(buffer.getStartIndex()).timestamp;
				windowBatchStart.setStartTimestamp(startTime);
				windowBatchStart.setEndTimestamp(startTime + windowDef.getSlide() * (this.numberOfWindowsInBatch-1) + windowDef.getSize());
			}
			else {
				this.currentElementIndexStart++;
				this.currentElementIndexEnd++;
			}
			
			
			/*
			 * Should we open new windows?
			 */
			// flag to check whether the timestamp of the current element actual indicates a new window
			boolean newWindow = false;
			while (element.timestamp - windowDef.getSlide() >= windowBatchStart.getStartTimestamp() + this.currentWindowStartPointer * windowDef.getSlide()) {
				this.currentWindowStartPointer++;
				newWindow |= true;
				
				// Is the new window part of the next window batch?
				if (this.currentWindowStartPointer >= this.numberOfWindowsInBatch) {
					// Store free indices for current batch
					freeUpTo.put(windowBatchStart, this.currentElementIndexStart-1);
					long startTimeForNextBatch = windowBatchStart.getStartTimestamp() + this.numberOfWindowsInBatch * windowDef.getSlide();
					// Create new window batch
					windowBatchStart = new BufferWindowBatch(buffer, freshInitializedArray(this.numberOfWindowsInBatch,-1),  freshInitializedArray(this.numberOfWindowsInBatch,-1));
					windowBatchStart.setStartTimestamp(startTimeForNextBatch);
					windowBatchStart.setEndTimestamp(startTimeForNextBatch + windowDef.getSlide() * (this.numberOfWindowsInBatch-1) + windowDef.getSize());
					this.windowBatches.add(windowBatchStart);
					this.windowBatchesNextForEnd.add(windowBatchStart);
					// Set the current window pointer to zero, i.e., the first window of the new batch
					this.currentWindowStartPointer = 0;
					
					// In the new window batch, we can use normalized indices again
					this.currentElementIndexStart = buffer.normIndex(this.currentElementIndexStart);
				}
			}
			if (newWindow)
				windowBatchStart.getWindowStartPointers()[this.currentWindowStartPointer] = this.currentElementIndexStart;


			if (this.currentWindowEndPointer == -1) 
				this.currentWindowEndPointer = 0;

			/*
			 * Should we close open windows?
			 */
			// flag to make sure that the end pointer is set only for the oldest window
			boolean oldWindow = true;
			while (element.timestamp > windowBatchEnd.getStartTimestamp() + this.currentWindowEndPointer * windowDef.getSlide() + windowDef.getSize()) {
				if (oldWindow)
					windowBatchEnd.getWindowEndPointers()[this.currentWindowEndPointer] = this.currentElementIndexEnd - 1;
				oldWindow = false;
				this.currentWindowEndPointer++;
				
				// Will the next window to close be part of the next window batch?
				if (this.currentWindowEndPointer >= this.numberOfWindowsInBatch) {
					// Move to new window batch
					windowBatchEnd = windowBatchesNextForEnd.poll();
					windowBatchesEnd.add(windowBatchEnd);
					// Set the current window pointer to zero, i.e., the first window of the new batch
					this.currentWindowEndPointer = 0;
					
					// In the new window batch, we can use normalized indices again
					this.currentElementIndexEnd = buffer.normIndex(this.currentElementIndexEnd);
					
					// Since we closed a window batch, we should check whether there is a new set of batches (for different streams) for a task
					this.connectable.getTaskDispatcher().assembleAndDispatchTask();
				}
			}
			
			break;

		default:
			LOG.error("Unknown window definition: {}", windowDef.getWindowType().toString());
			break;
		}
	}

	public Deque<IWindowBatch> getFullWindowBatches() { 
		return this.windowBatchesEnd;
	}
	
	public int getFreeIndexForBatchAndRemoveEntry(IWindowBatch batch) {
		return this.freeUpTo.remove(batch);
	}
	
	private int[] freshInitializedArray(int size, int init) {
		int[] result = new int[size];
		Arrays.fill(result, init);
		return result;
	}
	
}
