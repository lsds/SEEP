package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowResult {
	
	long id;
	
//	/* If this is a range, then we store the last window index */
//	long last;
//	
//	long freeIndex = -1;
//	
	WindowResult next;
	boolean marked;
	Lock lock;
	
	PaneSet partialResults = null;
	
	boolean opened = false;
	boolean closed = false;
	
	public WindowResult (long id) { // long last, long freeIndex) {
		
		this.id = id;
		// this.last = last;
		
		// this.freeIndex = freeIndex;
		
		this.next = null;
		this.marked = false;
		this.lock = new ReentrantLock();
	}
	
//	public void update (IQueryBuffer buffer, boolean opening, boolean closing, long freeIndex) {
//		
//		if (opening) 
//			this.opened = true;
//		
//		if (closing) {
//			this.closed = true;
//			this.freeIndex = freeIndex;
//		}
//	}
	
	public long getWindowId () {
		
		return this.id;
	}
	
//	public boolean isComplete () {
//		
//		return (opened && closed);
//	}
	
	public void lock () {
		
		this.lock.lock();
	}
	
	public void unlock () {
		
		this.lock.unlock();
	}
}
