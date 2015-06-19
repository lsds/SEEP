package uk.ac.imperial.lsds.seep.multi;

public class WindowMap {
	
	/* Note that the following value must be a power of two (see `hash`). */
	public static int WINDOW_MAP_CONTENT_SIZE = 1024;
	
	WindowTuple [] content;
	
	TupleSet heap;
	
	int size = 0;
	
	int id = -1;
	
	long autoIndex = -1;
	
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty () {
		
		return (this.size == 0);
	}
	
	public WindowMap (int id, long autoIndex) {
		
		content = new WindowTuple [WINDOW_MAP_CONTENT_SIZE];
		
		for (int i = 0; i < content.length; i++)
			content[i] = null;
		
		this.id = id;
		this.autoIndex = autoIndex;
		
		this.heap = new TupleSet (WINDOW_MAP_CONTENT_SIZE);
	}
	
	public WindowMap () {
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
	
	private int hash (int key) {
		
		return key & (WINDOW_MAP_CONTENT_SIZE - 1);
	}
	
	public boolean containsKey (int hashcode) {
		
		WindowTuple current = content[hash(hashcode)];
		
		if (current != null)
			return true;
		return false;
	}
	
	public WindowTuple containsKey (int hashcode, int keyOffset) {
		
		WindowTuple current = content[hash(hashcode)];
		
		while (current.compareTo(keyOffset) != 0 && current.next != null)
			current = current.next;
		
		if (current.compareTo(keyOffset) == 0)
			return current;
		
		return null;
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

	public WindowTuple [] getEntries() {
		
		return this.content;
	}
	
	public TupleSet getHeap() {
		
		return this.heap;
	}
	
	public void release () {
		
		clear();
		WindowMapFactory.free(this);
	}
	
	public String toString () {
		
		return String.format("[WindowMap %03d pool-%02d %6d items] ", autoIndex, id, size);
	}
}
