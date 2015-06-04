package uk.ac.imperial.lsds.seep.multi;


public class Pane {
	
	/* Note that the following value must be a power of two (see `hash`). */
	private static final int CONTENT_SIZE = 1024;
	
	IntermediateTuple [] contents;
	
	IntermediateTupleSet heap;
	
	int size = 0;
	
	int id = -1;
	
	long paneIndex = -1;
	
	int freeIndex = -1;
	
	public int size() {
		
		return this.size;
	}
	
	public boolean isEmpty () {
		
		return (this.size == 0);
	}
	
	public Pane (int id) {
		
		contents = new IntermediateTuple [CONTENT_SIZE];
		
		for (int i = 0; i < contents.length; i++)
			contents[i] = null;
		
		heap = new IntermediateTupleSet (CONTENT_SIZE);
		
		this.id = id;
	}
	
	public long getPaneIndex () {
		
		return this.paneIndex;
	}
	
	public void setPaneIndex (long paneIndex) {
		
		this.paneIndex = paneIndex;
	}
	
	public int getFreeIndex () {
		
		return this.freeIndex;
	}
	
	public void setFreeIndex (int freeIndex) {
		
		this.freeIndex = freeIndex;
	}
	
	public int getProcessorId () {
		
		return id;
	}
	
	public void setProcessorId (int id) {
		
		this.id = id;
	}
	
	public void put (long timestamp, Key key, float value, int count) {
		
		IntermediateTuple current = contents[hash(key)];
		
		if (current == null) {
			
			contents[hash(key)] = 
				IntermediateTupleFactory.newInstance(id, timestamp, key, value, count, null);
			size++;
			/* Heapify */
			heap.add(size, contents[hash(key)]);
		
		} else {
			
			while ((! current.key.eq(key)) && current.next != null)
				current = current.next;
			
			if (current.key.eq(key)) {
				
				/* Replace `value` and `count` */
				current.value = value;
				current.count = count;
			
			} else {
				
				current.next = IntermediateTupleFactory.newInstance(id, timestamp, key, value, count, null);
				size++;
				/* Heapify */
				heap.add(size, current.next);
			}
		}
	}
	
	public boolean containsKey (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		if (current == null)
			return false;
		
		while ((! current.key.eq(key)) && current.next != null)
			current = current.next;
		
		if (current.key.eq(key))
			return true;
		
		return false;
	}
	
	public IntermediateTuple get (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		while ((! current.key.eq(key)) && current.next != null)
			current = current.next;
		
		if (! current.key.eq(key)) {
			System.err.println("error: key not found in pane");
			System.exit(1);
		}
		
		return current;
	}
	
	private int hash (Key key) {
		
		return key.hash() & (CONTENT_SIZE - 1);
	}
	
	public int clear() {
		
		size = 0;
		int count = 0;
		
		for (int i = 0; i < CONTENT_SIZE; i++) {
			
			if (contents[i] != null) {
				
				IntermediateTuple e = contents[i];
				while (e != null) {
					IntermediateTuple f = e.next;
					e.release(id);
					count++;
					e = f;
				}
				contents[i] = null;
			}
		}
		return count;
	}
	
	/*
	 * We should never remove an item from a pane.
	 * 
	public void remove (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		IntermediateTuple previous = null;
		
		while ((! current.key.eq(key)) && current.next != null) {
			previous = current;
			current = current.next;
		}
		
		if (current.key.eq(key)) {
			
			if (previous == null) {
				
				if (current.next != null)
					contents[hash(key)] = current.next;
				else 
					contents[hash(key)] = null;
			} else {
				if (current.next != null) 
					previous.next = current.next;
				else
					previous.next = null;
			}
			current.release(id);
			size--;
		}
	}
	*/
	
	public IntermediateTuple [] getEntries() {
		
		return this.contents;
	}
	
	public void release () {
		
		heap.clear();
		
		clear();
		PaneFactory.free(this);
	}
	
	public String toString () {
		
		return String.format("[pane %010d pool-%02d %6d items] ", paneIndex, id, size);
	}
}
