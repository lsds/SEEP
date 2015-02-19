package uk.ac.imperial.lsds.streamsql.op.stateful;


public class IntMap {

	public static final int INTMAP_CONTENT_SIZE = 1000;
	
	IntMapEntry[] content;
	
	int size = 0;
	
	public int size() {
		return this.size;
	}
	
	public IntMap() {
		content = new IntMapEntry[INTMAP_CONTENT_SIZE];
	}
	
	public void put(int key, int value) {
		IntMapEntry current = content[hash(key)];
		
		if (current == null) {
			content[hash(key)] = IntMapEntryFactory.newInstance(key, value, null);
			size++;
		}
		else {
			while (current.key != key && current.next != null)
				current = current.next;
			
			if (current.key == key) {
				current.value = value;
			}
			else {
				current.next = IntMapEntryFactory.newInstance(key, value, null);
				size++;
			}
		}
	}
	
	public boolean containsKey(int key) {
		IntMapEntry current = content[hash(key)];
		
		if (current == null)
			return false;
		
		while (current.key != key && current.next != null)
			current = current.next;
		
		if (current.key == key)
			return true;
		
		return false;
	}

	
	public int get(int key) {
		IntMapEntry current = content[hash(key)];
		
		while (current.key != key && current.next != null)
			current = current.next;
		
		if (current.key != key)
			System.err.println("Error in IntMap");
		
		return current.value;
	}
	
	private int hash(int key) {
		return key & (INTMAP_CONTENT_SIZE-1);
	}
	
	public void clear() {
		size = 0;
		for (int i = 0; i < INTMAP_CONTENT_SIZE; i++) {
			if (content[i] != null) {
				IntMapEntry e = content[i];
				while (e.next != null) {
					IntMapEntry f = e.next;
					e.release();
					e = f;
				}
			}
		}
	}
	
	public void remove(int key) {
		IntMapEntry current = content[hash(key)];
		
		IntMapEntry previous = null;
		while (current.key != key && current.next != null) {
			previous = current;
			current = current.next;
		}
		
		if (current.key == key) {
			if (previous == null) {
				if (current.next != null) 
					content[hash(key)] = current.next;
				else 
					content[hash(key)] = null;
			}
			else {
				if (current.next != null) 
					previous.next = current.next;
				else
					previous.next = null;
			}
			current.release();
			size--;
		}
	}

//	public int[] keySet() {
//		int[] result = new int[size];
//		
//		int k = 0;
//		for (int i = 0; i < INTMAP_CONTENT_SIZE; i++) {
//			if (content[i] != null) {
//				IntMapEntry e = content[i];
//				result[k++] = e.key;
//				while (e.next != null) {
//					e = e.next;
//					result[k++] = e.key;
//				}
//			}
//		}
//		return result;
//	}

	public IntMapEntry[] getEntries() {
		return this.content;
	}
	
	public void release() {
		clear();
	}

}
