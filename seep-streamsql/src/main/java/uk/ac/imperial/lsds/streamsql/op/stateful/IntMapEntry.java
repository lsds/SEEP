package uk.ac.imperial.lsds.streamsql.op.stateful;

public class IntMapEntry {

	public int key;
	
	public int value;
	
	public IntMapEntry next;
	
	private long autoIndex = -1;

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
	
	public long getAutoIndex () {
		return this.autoIndex;
	}
	
	public void setAutoIndex (long autoIndex) {
		this.autoIndex = autoIndex;
	}
}
