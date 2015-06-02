package uk.ac.imperial.lsds.seep.multi;

public class UnsafeIntMapEntry {

	public int key;
	
	public int value;
	
	public UnsafeIntMapEntry next;
	
	public UnsafeIntMapEntry(int key, int value, UnsafeIntMapEntry next) {
		this.key = key;
		this.value = value;
		this.next = next;
	}
	
	public void set(int key, int value, UnsafeIntMapEntry next) {
		this.key = key;
		this.value = value;
		this.next = next;
	}
}
