package uk.ac.imperial.lsds.seep.multi;

public class IntermediateMapEntry {

	public int key;
	
	public float value;
	public int count;
	
	public IntermediateMapEntry next;
	
	public IntermediateMapEntry(int key, float value, int count, IntermediateMapEntry next) {
		this.key = key;
		
		this.value = value;
		this.count = count;
		
		this.next = next;
	}
	
	public void set(int key, float value, int count, IntermediateMapEntry next) {
		this.key = key;
		this.value = value;
		this.count = count;
		this.next = next;
	}
	
	public void release(int pid) {
		IntermediateMapEntryFactory.free(pid, this);
	}
}
