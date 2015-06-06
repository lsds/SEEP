package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TheWindowHeap {
	
	private static enum Status {
		
		EMPTY, AVAILABLE, BUSY
	};
	
	private static class WindowHeapNode {
		
		long key;
		Pane pane;
		
		Status status;
		long owner;
		
		Lock lock;
		
		public WindowHeapNode () {
			
			status = Status.EMPTY;
			lock = new ReentrantLock();
		}
		
		public void init (Pane pane) {
			
			this.key = pane.getPaneIndex();
			this.pane = pane;
			
			status = Status.BUSY;
			owner = Thread.currentThread().getId();
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
		
		public String toString() {
			
			String s;
			if (status == Status.AVAILABLE) {
				s = "AVAILABLE";
			} else if (status == Status.EMPTY) {
				s = "EMPTY";
			} else {
				s = "BUSY";
			}
			return String.format("[owner %3d] [index %10d] [status %10s]", owner, key, s);
		}
	}
	
	private WindowHeapNode [] heap;
	
	private static int capacity = 1048576;
	
	private static final int root  =  1;
	private static final int noone = -1;
	
	private Lock theLock;
	
	int next;
	
	public TheWindowHeap () {
		
		theLock = new ReentrantLock ();
		next = root;
		
		 heap = new WindowHeapNode [capacity + 1];
		 
		 for (int i = 0; i < capacity + 1; i++) {
			 heap[i] = new WindowHeapNode();
		 }
	}
	
	private void swap (WindowHeapNode x, WindowHeapNode y) {
		long k   = x.key;
		Pane p   = x.pane;
		Status s = x.status;
		long o   = x.owner;
		x.key    = y.key;
		x.pane   = y.pane;
		x.status = y.status;
		x.owner  = y.owner;
		y.key    = k;
		y.pane   = p;
		y.status = s;
		y.owner  = o;
	}
	
	public void add (Pane pane) {
		
		theLock.lock();
		
		int child = next++;
		heap[child].lock();
		heap[child].init(pane);
		
		theLock.unlock();
		heap[child].unlock();
		
		while (child > root) {
			
			int parent = child / 2;
			heap[parent].lock();
			heap[child].lock();
			int prev = child;
			try {
				if (heap[parent].status == Status.AVAILABLE && heap[child].amOwner()) {
					if (heap[child].key < heap[parent].key) {
						swap (heap[child], heap[parent]);
						child = parent;
					} else {
						heap[child].status = Status.AVAILABLE;
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
				heap[root].status = Status.AVAILABLE;
				heap[child].owner = noone;
			}
			heap[root].unlock();
		}
	}
	
	public long tryFirst () {
		
		long key = -1;
		
		theLock.lock();
		
		if (next <= 1) {
			theLock.unlock();
			return key;
		}
		
		heap[root].lock();
		
		key = heap[root].key;
		
		theLock.unlock();
		heap[root].unlock();
		
		return key;
	}
	
	public Pane remove () {
		
		theLock.lock();
		
		if (next <= 1) {
			theLock.unlock();
			return null;
		}
		
		int bottom = --next;
		heap[root].lock();
		heap[bottom].lock();
		
		theLock.unlock();
		
		Pane result = heap[root].pane;
		
		heap[root].status = Status.EMPTY;
		heap[root].owner = noone;
		
		swap(heap[bottom], heap[root]);
		
		heap[bottom].unlock();
		
		if (heap[root].status == Status.EMPTY) {
			heap[root].unlock();
			return result;
		}
		
		heap[root].status = Status.AVAILABLE;
		
		int child = 0;
		int parent = root;
		
		while (parent < heap.length / 2) {
			
			int left = parent * 2;
			int right = (parent * 2) + 1;
			heap[left].lock();
			heap[right].lock();
			if (heap[left].status == Status.EMPTY) {
				heap[right].unlock();
				heap[left].unlock();
				break;
			} else
			if (heap[right].status == Status.EMPTY || heap[left].key < heap[right].key) {
				heap[right].unlock();
				child = left;
			} else {
				heap[left].unlock();
				child = right;
			}
			if (heap[child].key < heap[parent].key && heap[child].status != Status.EMPTY) {
				
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
	
	public synchronized void dump () {
		theLock.lock();
		for (int i = 1; i < next; i++) {
			System.out.println(String.format("[DBG] [TheWindowHeap] [%04d] %s", i, heap[i]));
		}
		theLock.unlock();
	}
}
