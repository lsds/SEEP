package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class PeriodicWindowBatch implements IPeriodicWindowBatch {

	int startIndex = -1;
	int endIndex = -1;
	
	long startTimestamp = -1;
	long endTimestamp = -1;
	
	SubQueryBuffer buffer;
	
	IWindowDefinition windowDefinition;
	
	public PeriodicWindowBatch(IWindowDefinition windowDefinition, SubQueryBuffer buffer, int startIndex, int endIndex) {
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.startTimestamp = this.buffer.get(startIndex).getPayload().timestamp;
		this.endTimestamp = this.buffer.get(endIndex).getPayload().timestamp;
	}
	
	public PeriodicWindowBatch(IWindowDefinition windowDefinition, SubQueryBuffer buffer, int startIndex, int endIndex, long startTimestamp, long endTimestamp) {
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}

	
	@Override
	public Iterator<DataTuple> iterator() {
		return new TupleIterator(this);
	}

	@Override
	public int getStartIndex() {
		return this.startIndex;
	}

	@Override
	public int getEndIndex() {
		return this.endIndex;
	}


	@Override
	public List<DataTuple> getAllTuples() {
		return this.buffer.getAsList();
	}

	@Override
	public DataTuple get(int index) {
		return this.buffer.get(index);
	}

	@Override
	public Iterator<List<DataTuple>> windowIterator() {
		switch (this.windowDefinition.getWindowType()) {
		case RANGE_BASED:
			return new TimeBasedWindowIterator(this);
		case ROW_BASED:
			return new CountBasedWindowIterator(this);

		default:
			throw new UnsupportedOperationException("Cannot create window iterator for window of type: " + this.windowDefinition.getWindowType());
		}
	}

	@Override
	public IWindowDefinition getWindowDefinition() {
		return this.windowDefinition;
	}

	@Override
	public void setWindowDefinition(IWindowDefinition windowDefinition) {
		this.windowDefinition = windowDefinition;
	}

	@Override
	public void performIncrementalComputation(
			IMicroIncrementalComputation incrementalComputation, IWindowAPI api) {
		
		switch (this.windowDefinition.getWindowType()) {
		case RANGE_BASED:
			
			long currentTime = startTimestamp;
			
			for (int i = startIndex; i <= endIndex; i++) {

				DataTuple tuple = this.get(i);
				long nextTime = tuple.getPayload().timestamp;
				
				/* 
				 * check whether the tuple is in the current window or 
				 * whether we need to advance time before adding this tuple
				 */
				while (nextTime > currentTime + this.windowDefinition.getSize()) {
					incrementalComputation.evaluateWindow(api);
					currentTime += this.windowDefinition.getSlide();
					/*
					 * We advanced the current time, so we need to remove
					 * tuples that are no longer in the window
					 */
					for (int j = i; j >= startIndex; j--) {
						DataTuple old = this.get(j);
						if (currentTime > old.getPayload().timestamp)
							incrementalComputation.enteredWindow(old);
					}
					
				}
				incrementalComputation.enteredWindow(tuple);
			}
			break;
		case ROW_BASED:
			for (int i = startIndex; i <= endIndex; i++) {
				// push new data
				incrementalComputation.enteredWindow(this.get(i));
				
				// output result if window closed
				if (i % this.windowDefinition.getSize() == 0) {
					incrementalComputation.evaluateWindow(api);
					/*
					 * remove elements that have been part of this window, but
					 * are not part of the subsequent window
					 */
					for (int j = i - (int)this.windowDefinition.getSize(); 
							j <= i - (int)this.windowDefinition.getSlide(); i++) 
							incrementalComputation.exitedWindow(this.get(j));
				}
			}
			break;

		default:
			throw new UnsupportedOperationException("Cannot do incremental computation for window of type: " + this.windowDefinition.getWindowType());
		}

		
	}

	@Override
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	@Override
	public long getEndTimestamp() {
		return this.endTimestamp;
	}

	
}
