package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class SubQueryBuffer {

	private static final long serialVersionUID = 1L;
	
	/* State array */
	private transient DataTuple [] elements;
	
	/* Index of the first (oldest) element */
	private transient int start = 0;
	
	private transient int end = 0;
	
	private transient boolean full = false;
	
	 
	/* Maximum number of elements */
	private final int max;
	
	public SubQueryBuffer () {
		this(32);
	}
	
	public SubQueryBuffer(int size) {
		if (size <= 0)
			throw new IllegalArgumentException
			("Buffer size must be greater than 0.");
		
		elements = new DataTuple [size];
		max = elements.length;
	}
	
	public int size () {
		int size = 0;
		if (end  < start) {
			size = max - start + end;
		} else
		if (end == start) {
			size = (full ? max : 0);
		} else {
			size = end - start;
		}
		return size;
	}
	
	public boolean isEmpty () {
		return (size() == 0);
	}
	
	public boolean isFull () {
		return (size() == max);
	}
	
	public int maxSize () {
		return max;
	}
	
	public void clear () {
		full = false;
		start = 0;
		end = 0;
		Arrays.fill(elements, null);
	}

	public boolean add (DataTuple element) {
		return add(element, false);
	}

	public boolean add (DataTuple element, boolean blockIfNotEmpty) {
		if (isFull()) {
			remove();
		}
		return addElement(element);
	}
	
	public boolean addElement (DataTuple element) {
		if (full)
			return false;
		elements[end++] = element;
		if (end >= max)
			end = 0;
		if (end == start)
			full = true;
		return true;
	}
	
	public DataTuple get () {
		if (isEmpty())
			throw new BufferUnderflowException();
		
		return elements[start];
	}

	public DataTuple get (int i) {
		if (isEmpty())
			throw new BufferUnderflowException();
		
		return elements[i];
	}

	public DataTuple getFirst () { return get(); }
	
	public DataTuple getLast () {
		if (isEmpty())
			throw new BufferUnderflowException();
		
		return elements[end-1];
	}
	
	private DataTuple remove () {
		DataTuple element = elements[start];
		start ++;
		if (start >= max)
			start = 0;
		full = false;
		return element;
	}
	
	public DataTuple [] toArray () {
		int length = size();
		DataTuple [] copy = new DataTuple [length];
		int left, right;
		
		if (end > start) { /* Normal mode */
			System.arraycopy(elements, start, copy, 0, length);
		} else {
			/* Copy in two parts */
			left  = max - start;
			right = length - left;
			System.arraycopy(elements, start, copy, 0, left);
			System.arraycopy(elements, 0, copy, left, right);
		}
		
		return copy;
	}
	
	public List<DataTuple> toList() {
		List<DataTuple> copy = new ArrayList<>();
		
		int length = size();
		if (end > start) { /* Normal mode */
			for (int i = 0; i < length; i++)
				copy.add(elements[start+i]);
		} else {
			int left, right;
			/* Copy in two parts */
			left  = max - start;
			right = length - left;
			for (int i = 0; i < left; i++)
				copy.add(elements[start+i]);
			for (int i = 0; i < right; i++)
				copy.add(elements[i]);
		}
		
		return copy;
	}
	
	private int nextAvailableSlot() {
		int answer;
		if (isEmpty()) return 0;	
		if (end >= start) {
			answer = end;
		} else {
			answer = end + (max - start);
		}
		return answer;
	}
	
	public boolean equals (SubQueryBuffer buffer) {
		DataTuple [] a =   this.toArray();
		DataTuple [] b = buffer.toArray();
		return Arrays.equals(a, b);
	}
	
}
