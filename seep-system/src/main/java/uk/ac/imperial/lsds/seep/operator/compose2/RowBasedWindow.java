package uk.ac.imperial.lsds.seep.operator.compose2;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class RowBasedWindow implements Window {

	int start = -1;
	int end = -1;
	long size = -1;
	long slide = -1;
	
	MultiOpInputList inputList;
	MultiOpOutputList outputList;
	
	public RowBasedWindow(MultiOpInputList inputList, MultiOpOutputList outputList, int start, int end, long size, long slide) {
		this.inputList = inputList;
		this.outputList = outputList;
		this.start = start;
		this.end = end;
		this.size = size;
		this.slide = slide;
	}
	
	@Override
	public Iterator<DataTuple> iterator() {
		return new WindowIterator(this);
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
	public boolean isCountBased() {
		return true;
	}

	@Override
	public boolean isRangeBased() {
		return false;
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
	public List<DataTuple> getInputList() {
		return this.inputList;
	}

	@Override
	public boolean add(DataTuple e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(int index, DataTuple element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAll(Collection<? extends DataTuple> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends DataTuple> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataTuple get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<DataTuple> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<DataTuple> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataTuple remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataTuple set(int index, DataTuple element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DataTuple> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

}
