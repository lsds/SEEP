package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class StaticWindowBatchIterator implements Iterator<List<DataTuple>> {

	private List<DataTuple> flatInputList;
	private int[] windowStartPointers;

	int windowCursor;
	
	public StaticWindowBatchIterator(List<DataTuple> flatInputList, int[] windowStartPointers) {
		this.flatInputList = flatInputList;
		this.windowStartPointers = windowStartPointers;
		this.windowCursor = 0;
	}
	
	@Override
	public boolean hasNext() {
		return this.windowCursor != this.windowStartPointers.length;
	}

	@Override
	public List<DataTuple> next() {
		if (this.windowCursor >= this.windowStartPointers.length)
			throw new NoSuchElementException();

		int end = (this.windowCursor < this.windowStartPointers.length-1)? windowStartPointers[this.windowCursor+1] : this.flatInputList.size();
		return this.flatInputList.subList(windowStartPointers[this.windowCursor++], end);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
