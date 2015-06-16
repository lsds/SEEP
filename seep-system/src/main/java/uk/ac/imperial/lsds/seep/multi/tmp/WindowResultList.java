package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.concurrent.atomic.AtomicLong;

public class WindowResultList {
	
	private WindowResult head;
	private WindowResult tail;
	
	// private AtomicLong size;
	
	public WindowResultList () {
		
		head = new WindowResult (Long.MIN_VALUE); // , Long.MIN_VALUE, -1);
		tail = new WindowResult (Long.MAX_VALUE); // , Long.MAX_VALUE, -1);
		
		head.next = tail;
		
		// size = new AtomicLong (0L);
	}
	
	private boolean validate (WindowResult pred, WindowResult curr) {
		
		return (! pred.marked && ! curr.marked && pred.next == curr);
	}
	
	public boolean add (long id) {
		// System.out.println(String.format("[DBG] [WindowResultList] add %10d", id));
		while (true) {
			WindowResult pred = head;
			WindowResult curr = head.next;
			while (curr.id < id) {
				pred = curr;
				curr = curr.next;
			}
			pred.lock();
			try {
				curr.lock();
				try {
					if (validate(pred, curr)) {
						if (curr.id == id) {
							// curr.update(buffer, opening, closing, freeIndex);
							return false;
						} else {
							WindowResult node = new WindowResult (id); // , last, freeIndex);
							// node.update(buffer, opening, closing, freeIndex);
							node.next = curr;
							pred.next = node;
							
							// /size.incrementAndGet();
							
							return true;
						}
					}
				} finally {
					curr.unlock();
				}
			} finally {
				pred.unlock();
			}
		}
	}
	
	public boolean remove (long id) {
		
		while (true) {
			WindowResult pred = head;
			WindowResult curr = head.next;
			while (curr.id < id) {
				pred = curr;
				curr = curr.next;
			}
			pred.lock();
			try {
				curr.lock();
				try {
					if (validate(pred, curr)) {
						if (curr.id != id) {
							return false;
						} else {
							// if (curr.isComplete()){
							curr.marked = true;
							pred.next = curr.next;
							// size.decrementAndGet();
							return true;
							// } else {
							//	return null;
							//}
						}
					}
				} finally {
					curr.unlock();
				}
			} finally {
				pred.unlock();
			}
		}
	}
	
	public boolean contains (long id) {
		WindowResult curr = head;
		while (curr.id < id)
			curr = curr.next;
		return (curr.id == id && ! curr.marked); // && curr.isComplete());
	}

//	public long size() {
//		
//		return size.get();
//	}
}
