package uk.ac.imperial.lsds.seep.multi;


public class TheWindow {
	
	/* Note that the following value must be a power of two (see `hash`). */
	private static final int CONTENT_SIZE = 1024;
	private static final int PANESET_SIZE = 1024;
	
	IntermediateTuple [] contents;
	
	PaneSet heap;
	
	int size = 0;
	
	public int size() {
		
		return this.size;
	}
	
	public boolean isEmpty () {
		
		return (this.size == 0);
	}
	
	public TheWindow () {
		
		contents = new IntermediateTuple [CONTENT_SIZE];
		
		for (int i = 0; i < contents.length; i++)
			contents[i] = null;
		
		heap = new PaneSet (PANESET_SIZE);
	}
	
	public void put (long timestamp, Key key, float value, int count) {
		
		IntermediateTuple current = contents[hash(key)];
		
		if (current == null) {
			
			contents[hash(key)] = 
				IntermediateTupleFactory.newInstance(timestamp, key, value, count, null);
			size++;
		
		} else {
			
			while ((! current.key.eq(key)) && current.next != null)
				current = current.next;
			
			if (current.key.eq(key)) {
				
				/* Replace `value` and `count` */
				current.value = value;
				current.count = count;
			
			} else {
				
				current.next = IntermediateTupleFactory.newInstance(timestamp, key, value, count, null);
				size++;
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
			current.release();
			size--;
		}
	}
	
	public IntermediateTuple [] getEntries() {
		
		return this.contents;
	}
	
	public void release () {
		
		heap.clear();
		clear();
	}
	
	public String toString () {
		
		return String.format("[window %010d %6d items] ", windowIndex, size);
	}
}
