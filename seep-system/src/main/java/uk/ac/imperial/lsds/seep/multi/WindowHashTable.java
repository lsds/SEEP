package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class WindowHashTable {
	
	/* Note that the following value must be a power of two (see `hash`). */
	public static int WINDOW_MAP_CONTENT_SIZE = 2048 * 2 * 2 * 2 * 2 * 2;
	
	ByteBuffer content;
	
	int id = -1;
	
	long autoIndex = -1;
	
	int keyLength, valueLength, tupleLength = -1;

	int capacity = 0;
	
	public int capacity() {
		
		return this.capacity;
	}
	
	public boolean isInitialised () {
		
		return (this.capacity != 0);
	}
	
	public WindowHashTable (int id, long autoIndex) {
		
		content = ByteBuffer.allocate(WINDOW_MAP_CONTENT_SIZE);
		
		for (int i = 0; i < content.capacity(); i++)
			content.put((byte) 0);
		
		this.id = id;
		this.autoIndex = autoIndex;
		
		this.keyLength = this.valueLength = -1;
	}
	
	public WindowHashTable () {
		this(-1, -1);
	}
	
	public int getId () {
		return id;
	}
	
	public void setId (int id) {
		this.id = id;
	}
	
	public long getAutoIndex () {
		return autoIndex;
	}
	
	public void setTupleLength (int keyLength, int valueLength) {
		
		this.keyLength = keyLength;
		this.valueLength = valueLength;
		
		/* +occupancy (1), +timestamp (8), +count (4) */
		this.tupleLength = 
				1 << (32 - Integer.numberOfLeadingZeros((this.keyLength + this.valueLength + 15) - 1));
		
		this.capacity = WINDOW_MAP_CONTENT_SIZE / this.tupleLength;
	}
	
	/* Linear scan of the hash table */
	public int getNext (int h) {
		
		return (h & (this.capacity - 1)) * this.tupleLength;
	}
	
	public int getIndex (byte [] tupleKey, boolean [] found) {
		
		int h = HashCoding.jenkinsHash(tupleKey, 1) & (this.capacity - 1);
		int idx = h * this.tupleLength;
		
		int attempts = 0;
		while (attempts < capacity) {
			
			/* System.out.println(String.format("[DBG] h %3d index %6d", h, idx)); */
			
			byte mark = content.get(idx);
			if (mark == 1) {
				if (compare (tupleKey, idx) == 0) {
					found[0] = true;
					/* System.out.println(String.format("[DBG] OK; found", h, idx)); */
					return idx;
				}
			} else
			if (mark == 0) { 
				found[0] = false;
				/* System.out.println(String.format("[DBG] OK")); */
				return idx;
			}
			attempts ++;
			idx = getNext (++h);
		}
		/* System.out.println(String.format("[DBG] Error")); */
		return -1;
	}
	
	private int compare (byte [] tupleKey, int offset) {
		
		/* The first byte indicates occupancy; the next 8 are the timestamp */
		int n = (offset + 9) + tupleKey.length;
		
		for (int i = (offset + 9), j = 0; i < n; i++, j++) {
			byte v1 = this.content.get(i);
			byte v2 = tupleKey[j];
			if (v1 == v2)
				continue;
			if (v1 < v2)
				return -1;
			return +1;
		}
		return 0;	
	}
	
	public void clear () {
		for (int i = 0; i < WINDOW_MAP_CONTENT_SIZE; i += tupleLength) {
			content.put(i, (byte) 0);
		}
		capacity = 0;
		content.clear();
		/* System.out.println("[DBG] clear " + this); */
	}
	
	public ByteBuffer getBuffer () {
		
		return this.content;
	}
	
	public void release () {
		
		clear();
		WindowHashTableFactory.free(this);
	}
	
	public String toString () {
		
		return String.format("[WindowHashTable %03d pool-%02d %6d bytes/tuple %6d items] ", autoIndex, id, tupleLength, capacity);
	}
}
