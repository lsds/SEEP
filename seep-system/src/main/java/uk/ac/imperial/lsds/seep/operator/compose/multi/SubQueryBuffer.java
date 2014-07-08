package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.nio.BufferUnderflowException;
import java.security.InvalidParameterException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class SubQueryBuffer {

	private transient DataTuple [] elements;
	
	private transient int start = 0;
	
	private transient int end = 0;
	
	private transient boolean full = false;
	
	public SubQueryBuffer () {
		this(32);
	}
	
	public SubQueryBuffer(int size) {
		if (size <= 0)
			throw new IllegalArgumentException
			("Buffer size must be greater than 0.");
		
		elements = new DataTuple[size];
	}
	
	public synchronized int size () {
		int size = 0;
		if (end  < start) {
			size = elements.length - start + end;
		} else
		if (end == start) {
			size = (full ? elements.length : 0);
		} else {
			size = end - start;
		}
		return size;
	}
	
	private boolean insertElement(DataTuple element) {
		if (full)
			return false;
		elements[end++] = element;
		if (end >= elements.length)
			end = 0;
		if (end == start)
			full = true;
		return true;
	}
	
	public boolean add (DataTuple element) {
		if (full) {
			//TODO: fix me
		}
		return insertElement(element);
	}

	public void freeElement(int i) {
		
	}

	public void freeElements(int i, int numberOfElements) {
		
	}

	private boolean validIndex(int i) {
		if (i < 0) 
			return false;
		
		if ((end < start) && (i > end) && (i < start)) 
			return false;
			
		if ((start < end) && ((i > end) || (i < start))) 
			return false;

		return true;
	}
	
	public synchronized DataTuple get(int i) {
		if (size() == 0)
			throw new BufferUnderflowException();
		
		if (!validIndex(i))
			throw new InvalidParameterException();

		return elements[i];
//		return elements[(start + i) % elements.length];
	}

	
//	public DataTuple [] toArray () {
//		int length = size();
//		DataTuple [] copy = new DataTuple [length];
//		int left, right;
//		
//		if (end > start) { /* Normal mode */
//			System.arraycopy(elements, start, copy, 0, length);
//		} else {
//			/* Copy in two parts */
//			left  = elements.length - start;
//			right = length - left;
//			System.arraycopy(elements, start, copy, 0, left);
//			System.arraycopy(elements, 0, copy, left, right);
//		}
//		
//		return copy;
//	}
	
//	public List<DataTuple> toList() {
//		List<DataTuple> copy = new ArrayList<>();
//		
//		int length = size();
//		if (end > start) { /* Normal mode */
//			for (int i = 0; i < length; i++)
//				copy.add(elements[start+i]);
//		} else {
//			int left, right;
//			/* Copy in two parts */
//			left  = max - start;
//			right = length - left;
//			for (int i = 0; i < left; i++)
//				copy.add(elements[start+i]);
//			for (int i = 0; i < right; i++)
//				copy.add(elements[i]);
//		}
//		
//		return copy;
//	}
//	
//	private int nextAvailableSlot() {
//		int answer;
//		if (isEmpty()) return 0;	
//		if (end >= start) {
//			answer = end;
//		} else {
//			answer = end + (max - start);
//		}
//		return answer;
//	}
//	
//	public boolean equals (SubQueryBuffer buffer) {
//		DataTuple [] a =   this.toArray();
//		DataTuple [] b = buffer.toArray();
//		return Arrays.equals(a, b);
//	}
	
}
