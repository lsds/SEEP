package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.security.InvalidParameterException;
import java.util.Arrays;

import uk.ac.imperial.lsds.seep.GLOBALS;

public class SubQueryBuffer {

	public static final int SUB_QUERY_BUFFER_CAPACITY = Integer.valueOf(GLOBALS.valueFor("subQueryBufferCapacity"));

	private MultiOpTuple[] elements;	
	private boolean[] freeElements;	
	
	private int start = 0;
	private int end = 0;
	
	private boolean full = false;
	
	private Object lock = new Object();
	private Object internalLock = new Object();

	public synchronized Object getLock() {
		return lock;
	}

	public SubQueryBuffer () {
		this(SUB_QUERY_BUFFER_CAPACITY);
	}

	public boolean isFull() {
		return this.full;
	}

	public MultiOpTuple[] add(MultiOpTuple[] tuples) {
		for (int i = 0; i < tuples.length; i++) {
			if (!add(tuples[i])) {
				MultiOpTuple[] remaining = new MultiOpTuple[tuples.length - i];
				System.arraycopy(tuples, i, remaining, 0, tuples.length - i);
				return remaining;
			}
		}
		return new MultiOpTuple[0];
	}

	public SubQueryBuffer(int size) {
		if (size <= 0)
			throw new IllegalArgumentException
			("Buffer size must be greater than 0.");
		elements = new MultiOpTuple[size];
		freeElements = new boolean[size];
		Arrays.fill(freeElements, false);
	}

	public boolean isMoreRecentThan(int first, int second) {
		int nFirst = normIndex(first); 
		int nSecond = normIndex(second); 
		if (!validIndex(nFirst)||!validIndex(nSecond))
			throw new InvalidParameterException();
		
		if (start < end)
			return (first < second);
		else {
			// (end <= start)
			if ((first >= start) && (second >= start)) 
				return (first < second);
			if ((first < end) && (second < end)) 
				return (first < second);
			if ((first >= start) && (second < end)) 
				return false;
			// ((second >= start) && (first < end)) 
				return true;		
		}
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
	
	private boolean insertElement(MultiOpTuple element) {
		
		synchronized (internalLock) {
			elements[end] = element;
			end = normIndex(end + 1);
			
			if (end == start)
				full = true;
		}
		
		return true;			
	}
	
	public boolean add(MultiOpTuple element) {
		if (full)
			return false;
		
		return insertElement(element);
	}

	public void freeIndex(int i) {
		int nI = normIndex(i); 
		if (!validIndex(nI))
			throw new InvalidParameterException();
		
		freeElements[nI] = true;

		synchronized (internalLock) {
	
			while((freeElements[start]) && ((end != start) || (end == start && start == nI))) {
				int free = start;
				// first, move pointer
				start = normIndex(start+1);
				// second, reset the buffer
				elements[free] = null;
				freeElements[free] = false;
			}
			
			if ((end != start) || (end == start && start == nI))
				full = false;
		}
		
		synchronized (getLock()) {
//			System.out.println("notify from buffer");
			this.getLock().notifyAll();
		}
	}

	public void freeUpToIndex(int i) {
		int nI = normIndex(i); 
		int toFree = start;
		do {
			freeIndex(toFree);
			toFree = normIndex(toFree + 1);
		}
		while (toFree != nI);
	}

	public boolean validIndex(int i) {
		if (i < 0) 
			return false;
		
		if ((end < start) && (i > end) && (i < start)) 
			return false;
			
		if ((start < end) && ((i >= end) || (i < start))) 
			return false;

		return true;
	}
	
	/**
	 * Non-synchronised access of the i-th element in the
	 * array. The given index is the actual index
	 * 
	 * @param i
	 * @return
	 */
	public MultiOpTuple get(int i) {
		int nI = normIndex(i); 
		if (!validIndex(nI))
			throw new InvalidParameterException();
		
		return elements[nI];
	}
	
	public int getStartIndex() {
		return this.start;
	}

	public int getIndexBefore(int i, int diff) {
		i -= diff;
		return normIndex(i + this.elements.length);
	}
	
	public int getEndIndex() {
		return this.end;
	}

	public int capacity() {
		return this.elements.length;
	}

	public MultiOpTuple[] getArray() {
		return getArray(this.start, getIndexBefore(this.end,1));
	}

	public MultiOpTuple[] getArray(int startIndex, int endIndex) {
		int nStartIndex = normIndex(startIndex);
		int nEndIndex = normIndex(endIndex);
		if (nEndIndex > nStartIndex) { 
			/* Normal mode */
			int length = nEndIndex - nStartIndex + 1;
			MultiOpTuple[] copy = new MultiOpTuple[length];
			System.arraycopy(elements, nStartIndex, copy, nStartIndex, length);
			return copy;
		} else {
			/* Copy in two parts */
			int lengthFirst = this.elements.length - nStartIndex;
			int lengthSecond = nEndIndex + 1;
			MultiOpTuple[] copy = new MultiOpTuple[lengthFirst + lengthSecond];
			System.arraycopy(elements, nStartIndex, copy, 0, lengthFirst);
			System.arraycopy(elements, 0, copy, lengthFirst-1, lengthSecond);
			return copy;
		}
	}
	
}
