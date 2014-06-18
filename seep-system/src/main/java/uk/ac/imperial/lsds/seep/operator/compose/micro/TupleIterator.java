package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class TupleIterator implements Iterator<DataTuple> {

	private IWindowBatch w;
	
	int cursor;
	
	public TupleIterator(IWindowBatch w) {
		this.w = w;
		this.cursor = this.w.getStart();
	}
	
	@Override
	public boolean hasNext() {
		return this.cursor != this.w.getEnd();
	}

	@Override
	public DataTuple next() {
		if (this.cursor >= this.w.getEnd())
			throw new NoSuchElementException();
		
		cursor++;
		return this.w.get(cursor-1);	
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
