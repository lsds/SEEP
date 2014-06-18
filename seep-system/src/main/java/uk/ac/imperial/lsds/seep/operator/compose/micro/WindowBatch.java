package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpInputList;

public class WindowBatch implements IWindowBatch {

	public enum WindowType  {
		ROW_BASED, RANGE_BASED
	}
	
	int start = -1;
	int end = -1;
	long size = -1;
	long slide = -1;
	
	MultiOpInputList inputList;
	
	WindowType windowType;
	
	public WindowBatch(WindowType windowType, MultiOpInputList inputList, int start, int end, long size, long slide) {
		this.inputList = inputList;
		this.windowType = windowType;
		this.start = start;
		this.end = end;
		this.size = size;
		this.slide = slide;
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
	public long getSize() {
		return this.size;
	}

	@Override
	public long getSlide() {
		return this.slide;
	}

	@Override
	public List<DataTuple> getAllTuples() {
		return this.inputList;
	}

	@Override
	public DataTuple get(int index) {
		return this.inputList.get(index);
	}

	@Override
	public Iterator<List<DataTuple>> windowIterator() {
		switch (this.windowType) {
		case RANGE_BASED:
			return new TimeBasedWindowIterator(this);
		case ROW_BASED:
			return new CountBasedWindowIterator(this);

		default:
			throw new UnsupportedOperationException("Cannot create window iterator for window of type: " + this.windowType);
		}
	}

	@Override
	public WindowType getWindowType() {
		return this.windowType;
	}

}
