package uk.ac.imperial.lsds.seep.multi;

public class ThreadMapNode {
	
	public long key;
	public int value;
	
	public ThreadMapNode next;
	
	public ThreadMapNode (long key, int value, ThreadMapNode next) {
		
		this.key = key;
		this.value = value;
		this.next = next;
	}
	
	public ThreadMapNode (long key, int value) {
		
		this (key, value, null);
	}
}
