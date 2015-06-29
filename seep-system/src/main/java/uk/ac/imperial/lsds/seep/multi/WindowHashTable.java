package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class WindowHashTable {
	
	/* Note that the following value must be a power of two (see `hash`). */
	public static int WINDOW_MAP_CONTENT_SIZE = 1048576;
	
	ByteBuffer content;
	
	int size = 0;
	
	int id = -1;
	
	long autoIndex = -1;
	
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty () {
		
		return (this.size == 0);
	}
	
	public WindowHashTable (int id, long autoIndex) {
		
		content = ByteBuffer.allocate(WINDOW_MAP_CONTENT_SIZE);
		
		for (int i = 0; i < content.capacity(); i++)
			content.put((byte) -1);
		
		this.id = id;
		this.autoIndex = autoIndex;
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
	
	public int getNext (int index) {
		
		return (index + this.tupleLength) & (WINDOW_MAP_CONTENT_SIZE - 1);
	}
	
	public int containsKey (byte [] tupleKey) {
		
		int idx = HashCoding.jenkinsHash(tupleKey, 1);
		idx &= (WINDOW_MAP_CONTENT_SIZE - 1);
		
		if (content.get(idx) == -1 || compare (tupleKey, idx) != 0)
			return -1;
		
		return idx;
	}
	
	private int compare(byte[] tupleKey, int offset) {
		int n = offset + tupleKey.length;
		
		for (int i = offset, j = 0; i < n; i++, j++) {
			
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
	
	public void put (int hashcode, IQueryBuffer buffer, int offset, int length, float value, int count) {
		
		WindowTuple t = WindowTupleFactory.newInstance(id, hashcode, buffer, offset, length, value, count, null);
		
		WindowTuple current = content[hash(hashcode)];
		
		if (current == null) {
			content[hash(hashcode)] = t;
		} else {
			while (current.next != null) current = current.next;
			current.next = t;
		}
		size++;
		
		this.heap.add(t);
	}
	
	public int clear() {
		size = 0;
		int count = 0;
		for (int i = 0; i < WINDOW_MAP_CONTENT_SIZE; i++) {
			if (content[i] != null) {
				WindowTuple e = content[i];
				while (e != null) {
					WindowTuple f = e.next;
					e.release(id); 
					count++;
					e = f;
				}
				content[i] = null;
			}
		}
		return count;
	}

	public ByteBuffer getEntries() {
		
		return this.content;
	}
	
	public void release () {
		
		clear();
		WindowHashTableFactory.free(this);
	}
	
	public String toString () {
		
		return String.format("[WindowHashTable %03d pool-%02d %6d items] ", autoIndex, id, size);
	}
}
