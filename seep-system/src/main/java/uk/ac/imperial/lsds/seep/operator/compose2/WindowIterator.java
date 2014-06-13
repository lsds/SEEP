package uk.ac.imperial.lsds.seep.operator.compose2;

import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class WindowIterator implements Iterator<DataTuple> {

	private Window w;
	
	int cursor;
	int lastRet = -1;
	
	public WindowIterator(Window w) {
		this.w = w;
		this.cursor = 0;
	}
	
	@Override
	public boolean hasNext() {
		return this.cursor != this.w.getEnd();
	}

	@Override
	public DataTuple next() {
		if (this.cursor >= this.w.getEnd())
			throw new NoSuchElementException();
		
		lastRet = cursor;
		cursor++;
		return this.w.get(lastRet);	
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
