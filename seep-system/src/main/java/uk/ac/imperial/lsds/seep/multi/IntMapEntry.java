package uk.ac.imperial.lsds.seep.multi;

public class IntMapEntry {

	public int key;
	
	public int value;
	
	public IntMapEntry next;
	
	public IntMapEntry(int key, int value, IntMapEntry next) {
		this.key = key;
		this.value = value;
		this.next = next;
	}
	
	public void set(int key, int value, IntMapEntry next) {
		this.key = key;
		this.value = value;
		this.next = next;
	}
	
	public void release(int pid) {
		IntMapEntryFactory.free(pid, this);
	}
}
