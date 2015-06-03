package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowChunk {
	
	WindowStateStatus status;
	long start, end;
	IntermediateMap result;
	long owner;
	Lock lock;
	
	public void init (long start, long end, IntermediateMap result) {
		this.start = start;
		this.end = end;
		this.result = result;
		status = WindowStateStatus.BUSY;
		owner = Thread.currentThread().getId();
	}
	
	public WindowChunk () {
		status = WindowStateStatus.EMPTY;
		lock = new ReentrantLock();
	}
	
	public void lock () {
		lock.lock();
	}
	
	public void unlock () {
		lock.unlock();
	}
	
	public boolean amOwner () {
		return (Thread.currentThread().getId() == owner);
	}
}
