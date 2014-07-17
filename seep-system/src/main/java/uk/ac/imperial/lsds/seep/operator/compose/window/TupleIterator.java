package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class TupleIterator implements Iterator<DataTuple> {

	private IPeriodicWindowBatch w;
	
	int cursor;
	
	public TupleIterator(IPeriodicWindowBatch w) {
		this.w = w;
		this.cursor = this.w.getStartIndex();
	}
	
	@Override
	public boolean hasNext() {
		return this.cursor != this.w.getEndIndex();
	}

	@Override
	public DataTuple next() {
		if (this.cursor >= this.w.getEndIndex())
			throw new NoSuchElementException();
		
		cursor++;
		return this.w.get(cursor-1);	
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
