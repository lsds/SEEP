package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class WindowHashTableWrapper {
	
	ByteBuffer content;
	int start, end;
	int tupleLength;
	int capacity;
	
	public int capacity() {
		return this.capacity;
	}
	
	public boolean isInitialised () {
		return (this.capacity != 0);
	}
	
	public WindowHashTableWrapper () {
		content = null;
		start = end = 0;
		tupleLength = 0;
		capacity = 0;
	}
	
	public void configure (ByteBuffer content, int start, int end, int tupleLength) {
		this.content = content;
		this.start = start;
		this.end = end;
		this.tupleLength = tupleLength;
		this.capacity = (end - start) / tupleLength;
	}
	
	/* Linear scan of the hash table */
	public int getNext (int h) {
		return (start + (h & (this.capacity - 1)) * this.tupleLength);
	}
	
	public int getIndex (byte [] array, int offset, int length, boolean [] found) {
		int h = HashCoding.jenkinsHash(array, offset, length, 1) & (this.capacity - 1);
		int idx = start + h * this.tupleLength;
		
		int attempts = 0;
		while (attempts < capacity) {
			
			/* System.out.println(String.format("[DBG] h %3d index %6d", h, idx)); */
			
			if (content.get(idx) == 1) {
				if (compare (array, offset, length, idx) == 0) {
					found[0] = true;
					/* System.out.println(String.format("[DBG] OK; found", h, idx)); */
					return idx;
				}
			} else {
				found[0] = false;
				/* System.out.println(String.format("[DBG] OK")); */
				return idx;
			}
			attempts ++;
			idx = getNext (++h);
		}
		System.out.println(String.format("[DBG] Error"));
		return -1;
	}
	
	private int compare (byte [] array, int offset, int length, int index) {
		
		/* The first byte indicates occupancy; the next 8 are the timestamp */
		int n = (index + 9) + length;
		
		for (int i = (index + 9), j = offset; i < n; i++, j++) {
			byte v1 = this.content.get(i);
			byte v2 = array[j];
			if (v1 == v2)
				continue;
			if (v1 < v2)
				return -1;
			return +1;
		}
		return 0;	
	}
}
