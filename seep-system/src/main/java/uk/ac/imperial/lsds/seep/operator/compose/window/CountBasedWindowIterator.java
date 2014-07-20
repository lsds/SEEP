package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class CountBasedWindowIterator implements Iterator<List<DataTuple>> {

	private IPeriodicWindowBatch w;
	
	int windowCursor;
	int numberOfWindows;
	
	public CountBasedWindowIterator(IPeriodicWindowBatch w) {
		this.w = w;
		this.windowCursor = 0;
		this.numberOfWindows = (int) Math.floor((1.0*(this.w.getEndIndex() - this.w.getStartIndex() - this.w.getWindowDefinition().getSize() + 1))/this.w.getWindowDefinition().getSlide());
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
		int start = this.w.getStartIndex() + windowCursor * (int)this.w.getWindowDefinition().getSlide();
		for (int i = start; i < start + this.w.getWindowDefinition().getSize(); i++)
			window.add(this.w.get(i));
		
		windowCursor++;
		return window;	
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
