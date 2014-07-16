package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class PeriodicWindowBatch implements IPeriodicWindowBatch {

	int start = -1;
	int end = -1;
	
	SubQueryBuffer buffer;
	
	IWindowDefinition windowDefinition;
	
	public PeriodicWindowBatch(IWindowDefinition windowDefinition, SubQueryBuffer buffer, int start, int end) {
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public Iterator<DataTuple> iterator() {
		return new TupleIterator(this);
	}

	@Override
	public int getStart() {
		return this.start;
	}

	@Override
	public int getEnd() {
		return this.end;
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

	
}
