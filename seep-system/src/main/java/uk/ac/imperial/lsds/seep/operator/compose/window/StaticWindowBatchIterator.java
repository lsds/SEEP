package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		int end = (this.windowCursor != this.windowStartPointers.length)? windowStartPointers[this.windowCursor+1] : this.windowStartPointers.length;
		end--;
		return new ArrayList<>(this.flatInputList.subList(windowStartPointers[this.windowCursor], end));
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
