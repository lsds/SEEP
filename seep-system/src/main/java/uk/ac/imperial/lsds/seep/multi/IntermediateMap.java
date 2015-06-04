package uk.ac.imperial.lsds.seep.multi;


public class IntermediateMap {
	
	/* Note that the following value must be a power of two (see `hash`). */
	public static final int INTERMEDIATEMAP_CONTENT_SIZE = 1024;
	
	IntermediateMapEntry [] content;
	
	int size = 0;
	
	int id = -1;
	
	long autoIndex = -1;
	
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty () {
		return (this.size == 0);
	}
	
	public IntermediateMap (int id, long autoIndex) {
		content = new IntermediateMapEntry[INTERMEDIATEMAP_CONTENT_SIZE];
		for (int i = 0; i < content.length; i++)
			content[i] = null;
		this.id = id;
		this.autoIndex = autoIndex;
	}
	
	public IntermediateMap () {
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

	public void put(int key, float value, int valueCount) {
		
		/* System.out.println ("[DBG] put in IntMap: " + key + " hash is " + hash(key) + " size is " + size); */
		
		IntermediateMapEntry current = content[hash(key)];
		
		if (current == null) {
			content[hash(key)] = IntermediateMapEntryFactory.newInstance(id, key, value, valueCount, null);
			size++;
		}
		else {
			while (current.key != key && current.next != null)
				current = current.next;
			
			if (current.key == key) {
				current.value = value;
			}
			else {
				current.next = IntermediateMapEntryFactory.newInstance(id, key, value, valueCount, null);
				size++;
			}
		}
	}
	
	public boolean containsKey(int key) {
		IntermediateMapEntry current = content[hash(key)];
		
		if (current == null)
			return false;
		
		while (current.key != key && current.next != null)
			current = current.next;
		
		if (current.key == key)
			return true;
		
		return false;
	}

	public IntermediateMapEntry get(int key) {
		IntermediateMapEntry current = content[hash(key)];
		
		while (current.key != key && current.next != null)
			current = current.next;
		
		if (current.key != key) {
			System.err.println("error: key not found in IntermediateMap");
			System.exit(1);
		}
		
		return current;
	}
	
	private int hash(int key) {
		return key & (INTERMEDIATEMAP_CONTENT_SIZE-1);
	}
	
	public int clear() {
		size = 0;
		int count = 0;
		for (int i = 0; i < INTERMEDIATEMAP_CONTENT_SIZE; i++) {
			if (content[i] != null) {
				IntermediateMapEntry e = content[i];
				while (e != null) {
					IntermediateMapEntry f = e.next;
					e.release(id); 
					count++;
					e = f;
				}
				content[i] = null;
			}
		}
		return count;
	}
	
	public void remove(int key) {
		IntermediateMapEntry current = content[hash(key)];
		
		IntermediateMapEntry previous = null;
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
			current.release(id);
			size--;
		}
	}
	
	/*
	public int[] keySet() {
		int[] result = new int[size];
		
		int k = 0;
		for (int i = 0; i < INTMAP_CONTENT_SIZE; i++) {
			if (content[i] != null) {
				IntMapEntry e = content[i];
				result[k++] = e.key;
				while (e.next != null) {
					e = e.next;
					result[k++] = e.key;
				}
			}
		}
		return result;
	}
	*/

	public IntermediateMapEntry [] getEntries() {
		return this.content;
	}
	
	public void release () {
		clear();
		IntermediateMapFactory.free(this);
	}
	
	public String toString () {
		
		return String.format("[IntermediateMap %03d pool-%02d %6d items] ", autoIndex, id, size);
	}
	
	public void intersect (IntermediateMap x) {
		
		if (x == null)
			return;
		
		if (this.size == 0) {
			
			for (int k = 0; k < x.content.length; k++) {
				
				IntermediateMapEntry e = x.content[k];
			
				while (e != null) {
					
					put (e.key, e.value, e.count);
					
					e = e.next;
				}
			}
			
		} else {
		
			for (int k = 0; k < content.length; k++) {
			
				IntermediateMapEntry e = content[k];
			
				while (e != null) {
				
					IntermediateMapEntry otherentry = x.get(e.key);
					e.value += otherentry.value;
					e.count += otherentry.count; /* TODO: Add count properly */
				
					e = e.next;
				}
			}
		}
	}

	public void populate(IQueryBuffer outBuffer) {
		
		for (int k = 0; k < content.length; k++) {
			
			IntermediateMapEntry e = content[k];
			
			while (e != null) {
				
				outBuffer.putLong(1);
				outBuffer.putInt(e.key);
				// if (aggregationType == AggregationType.AVG) {
				//	outBuffer.putFloat(e.value / (float) e.count);
				// }
				// else {
					outBuffer.putFloat(e.value);
				// }
				e = e.next;
			}
		}
	}

	public void clear(IntermediateMap currentWindow) {
		
		
	}
}
