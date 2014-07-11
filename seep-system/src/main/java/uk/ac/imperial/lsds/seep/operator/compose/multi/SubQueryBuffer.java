package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.security.InvalidParameterException;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class SubQueryBuffer {

	private DataTuple [] elements;
	
	private int start = 0;
	
	private int end = 0;
	
	private boolean full = false;

	public SubQueryBuffer () {
		this(1024);
	}

	public void pushData(List<DataTuple> tuples, int streamID) {
		for (DataTuple tuple : tuples)
			pushData(tuple, streamID);
	}

	public void pushData(DataTuple tuple, int streamID) {
		this.inputQueues.get(streamID).add(tuple);
	}
	
	public void pushDataToAllStreams(DataTuple data) {
		for (Integer streamID : this.inputQueues.keySet())
			pushData(data, streamID);
	};

	
	public SubQueryBuffer(int size) {
		if (size <= 0)
			throw new IllegalArgumentException
			("Buffer size must be greater than 0.");
		elements = new DataTuple[size];
	}
	
	public int normIndex(int i) {
		return (i%elements.length);
	}

	public int normEndOfRange(int start, int end) {
		if (start >= end)
			return end;

		int nEnd = end%elements.length;
		while (nEnd < start)
			nEnd += elements.length;
		
		return nEnd;
	}

//	private int size () {
//		int size = 0;
//		if (end  < start) 
//			size = elements.length - start + end;
//		else if (end == start) 
//			size = (full ? elements.length : 0);
//		else 
//			size = end - start;
//		
//		return size;
//	}
	
	private boolean insertElement(DataTuple element) {
		end = normIndex(end++);

		elements[end] = element;
		
		if (end == start)
			full = true;
		
		return true;			
	}
	
	public boolean add(DataTuple element) throws InterruptedException {
		while (full)
			wait();
		
		return insertElement(element);
	}

	public void freeElement(int i) {
		int nI = normIndex(i); 
		if (!validIndex(nI))
			throw new InvalidParameterException();
		
		elements[nI] = null;
		while(elements[normIndex(start)] == null)
			start++;
		
		start = normIndex(start);
		if (end != start)
			full = false;
		notify();
	}

	public synchronized void freeElements(int i, int numberOfElements) {
		for (int j = 0; j < numberOfElements; j++) 
			freeElement(i+j);
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

}
