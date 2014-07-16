package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class SubQueryBuffer {

	public static final int SUB_QUERY_BUFFER_CAPACITY = Integer.valueOf(GLOBALS.valueFor("subQueryBufferCapacity"));

	private DataTuple [] elements;
	
	private int start = 0;
	
	private int end = 0;
	
	private boolean full = false;

	public SubQueryBuffer () {
		this(SUB_QUERY_BUFFER_CAPACITY);
	}

	public List<DataTuple> add(List<DataTuple> tuples) {
		Iterator<DataTuple> iter = tuples.iterator();
		while (iter.hasNext()) {
			if (add(iter.next()))
				iter.remove();
		}
		return tuples;
	}

	public SubQueryBuffer(int size) {
		if (size <= 0)
			throw new IllegalArgumentException
			("Buffer size must be greater than 0.");
		elements = new DataTuple[size];
	}
	
	public int normIndex(int i) {
		return (i%elements.length);
	}

	public int size () {
		int size = 0;
		if (end  < start) 
			size = elements.length - start + end;
		else if (end == start) 
			size = (full ? elements.length : 0);
		else 
			size = end - start;
		
		return size;
	}
	
	private boolean insertElement(DataTuple element) {
		elements[normIndex(end++)] = element;
		
		end = normIndex(end);
		
		if (end == start)
			full = true;
		
		return true;			
	}
	
	public boolean add(DataTuple element) {
		if (full)
			return false;
		
		return insertElement(element);
	}

	public void freeIndex(int i) {
		int nI = normIndex(i); 
		if (!validIndex(nI))
			throw new InvalidParameterException();
		
		elements[nI] = null;
		while((elements[normIndex(start)] == null) && (normIndex(start) != end))
			start++;
		
		start = normIndex(start);
		if (end != start)
			full = false;
		this.notifyAll();
	}

	public void freeUpToIndex(int i) {
		int nI = normIndex(i); 
		for (int j = start; normIndex(j) < nI; j++) 
			freeIndex(j);
	}

	private boolean validIndex(int i) {
		if (i < 0) 
			return false;
		
		if ((end < start) && (i > end) && (i < start)) 
			return false;
			
		if ((start < end) && ((i > end) || (i < start))) 
			return false;

		if (end == start && !full)
			return false;
		
		return true;
	}
	
	/**
	 * Non-synchronised access of the i-th element in the
	 * array. The given index is the actual index, not a Assumes that the given index is a position 
	 * that 
	 * 
	 * @param i
	 * @return
	 */
	public DataTuple get(int i) {
		int nI = normIndex(i); 
		if (!validIndex(nI))
			throw new InvalidParameterException();
		return elements[nI];
	}
	
	public int getStartIndex() {
		return this.start;
	}
	
	public int getEndIndex() {
		return this.end;
	}

	public int capacity() {
		return this.elements.length;
	}

	public List<DataTuple> getAsList() {
		List<DataTuple> result = new ArrayList<>();
		for (int i = start; normIndex(i) < end; i++)
			result.add(this.get(i));
		return result;
	}
	
}
