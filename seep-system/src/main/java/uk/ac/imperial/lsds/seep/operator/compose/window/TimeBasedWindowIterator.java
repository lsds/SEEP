package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class TimeBasedWindowIterator implements Iterator<List<DataTuple>> {

	private IWindowBatch w;
	
	int windowCursor;
	int numberOfWindows;
	int lastRet = -1;
	long tsWindowBatchStart;
	
	public TimeBasedWindowIterator(IWindowBatch w) {
		this.w = w;
		this.windowCursor = 0;
		tsWindowBatchStart = this.w.get(this.w.getStart()).getPayload().timestamp;
		long tsWindowBatchEnd = this.w.get(this.w.getEnd()).getPayload().timestamp;
		
		this.numberOfWindows = (int) Math.floor((1.0*(tsWindowBatchEnd - tsWindowBatchStart - this.w.getWindowDefinition().getSize()))/this.w.getWindowDefinition().getSlide());
	}
	
	@Override
	public boolean hasNext() {
		return this.windowCursor != this.numberOfWindows;
	}

	@Override
	public List<DataTuple> next() {
		if (this.windowCursor >= this.numberOfWindows)
			throw new NoSuchElementException();
		
		List<DataTuple> window = new ArrayList<>();
		
		long tsWindowEnd = tsWindowBatchStart + windowCursor * this.w.getWindowDefinition().getSlide() + this.w.getWindowDefinition().getSize();
		DataTuple currentTuple = (lastRet == -1)? this.w.get(this.w.getStart()) : this.w.get(lastRet);
		while (currentTuple.getPayload().timestamp <= tsWindowEnd) {
			window.add(currentTuple);
			currentTuple = this.w.get(lastRet++);
		}
		
		windowCursor++;
		return window;	
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
