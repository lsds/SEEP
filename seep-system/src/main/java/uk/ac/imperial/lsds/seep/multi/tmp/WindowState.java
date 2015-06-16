package uk.ac.imperial.lsds.seep.multi.tmp;

import java.nio.BufferOverflowException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowState {
	
	private long start, end;
	private boolean completed = false;
	
	private WindowChunk [] heap;
	
	private static int capacity = 1048576;
	
	private static int root = 1;
	private static int noone = -1;
	private Lock heapLock;
	int next;
	
	long lastInserted = -1;
	long lastRemoved  = -1;
	
	public WindowState (long start, long end) {
		
		this.start = start;
		this.end = end;
		
		heapLock = new ReentrantLock ();
		next = root;
		
		 heap = new WindowChunk [capacity + 1];
		 
		 for (int i = 0; i < capacity + 1; i++) {
			 heap[i] = new WindowChunk();
		 }
	}
	
	private void swap (WindowChunk p, WindowChunk q) {
		
		long start = p.start;
		long end = p.end;
		
		IntermediateMap result = p.result;
		
		WindowStateStatus status = p.status;
		
		long owner = p.owner;
		
		p.start = q.start;
		p.end = q.end;
		p.result = q.result;
		p.status = q.status;
		p.owner = q.owner;
		
		q.start = start;
		q.end = end;
		q.result = result;
		q.status = status;
		q.owner = owner;
	}
	
	public void add (long start, long end, IntermediateMap result) {
		
		heapLock.lock();
		
		// System.out.println(String.format("[DBG] [ADD] thread %3d got the heap lock", Thread.currentThread().getId()));
		
		lastInserted = start;
		
		int child = next++;
		heap[child].lock();
		heap[child].init(start, end, result);
		
		// System.out.println(heap[child]);
		
		heapLock.unlock();
		heap[child].unlock();
		
		while (child > root) {
			
			/* Loop */
			// System.out.println("Looping...");
			
			int parent = child / 2;
			heap[parent].lock();
			heap[child].lock();
			int prev = child;
			try {
				if (heap[parent].status == WindowStateStatus.AVAILABLE && heap[child].amOwner()) {
					if (heap[child].start < heap[parent].start) {
						swap (heap[child], heap[parent]);
						child = parent;
					} else {
						heap[child].status = WindowStateStatus.AVAILABLE;
						heap[child].owner = noone;
						return;
					}
				} else
				if (! heap[child].amOwner()) {
					child = parent;
				}
			} finally {
				heap[prev].unlock();
				heap[parent].unlock();
			}
		}
		
		if (child == root) {
			heap[root].lock();
			if (heap[root].amOwner()) {
				heap[root].status = WindowStateStatus.AVAILABLE;
				heap[child].owner = noone;
			}
			heap[root].unlock();
		}
	}
	
	public IntermediateMap remove (long index) {
		
		// System.out.println("Remove..." + index);
		
		heapLock.lock();
		// System.out.println(String.format("[DBG] [DEL] thread %3d got the heap lock", Thread.currentThread().getId()));
		// lastRemoved = heap[root].start;
		if (heap[root].start != index) {
			heapLock.unlock();
			return null;
		}
		if (next <= 1) {
			heapLock.unlock();
			return null;
		}
		
		int bottom = --next;
		heap[root].lock();
		heap[bottom].lock();
		
		heapLock.unlock();
		
		IntermediateMap result = heap[root].result;
		
		heap[root].status = WindowStateStatus.EMPTY;
		heap[root].owner = noone;
		
		swap(heap[bottom], heap[root]);
		// heap[root].start = heap[bottom].start;
		// heap[root].end = heap[bottom].start;
		// heap[root].result = heap[bottom].result;
		// heap[root].status = heap[bottom].status;
		// heap[root].owner = heap[bottom].owner;
		
		heap[bottom].unlock();
		
		if (heap[root].status == WindowStateStatus.EMPTY) {
			heap[root].unlock();
			return result;
		}
		
		// int len = next;
		/*************/
		heap[root].status = WindowStateStatus.AVAILABLE;
		/**************/
		int child = 0;
		int parent = root;
		
		while (parent < heap.length / 2) {
			
			int left = parent * 2;
			int right = (parent * 2) + 1;
			heap[left].lock();
			heap[right].lock();
			if (heap[left].status == WindowStateStatus.EMPTY) {
				heap[right].unlock();
				heap[left].unlock();
				break;
			} else
			if (heap[right].status == WindowStateStatus.EMPTY || heap[left].start < heap[right].start) {
				heap[right].unlock();
				child = left;
			} else {
				heap[left].unlock();
				child = right;
			}
			if (heap[child].start < heap[parent].start && heap[child].status != WindowStateStatus.EMPTY) {
				
				swap (heap[parent], heap[child]);
				
				heap[parent].unlock();
				parent = child;
			} else {
				heap[child].unlock();
				break;
			}
		}
		heap[parent].unlock();
		return result;
	}
	
	public long getLastInserted () {
		return lastInserted;
	}
	
	public long getLastRemoved () {
		return lastRemoved;
	}
	
	public boolean isComplete () {
		return completed;
	}
	
	public synchronized void dump () {
		heapLock.lock();
		for (int i = 1; i < next; i++) {
			System.out.println(String.format("[HEAP] [%3d] %s", i, heap[i]));
		}
		heapLock.unlock();
	}
}
