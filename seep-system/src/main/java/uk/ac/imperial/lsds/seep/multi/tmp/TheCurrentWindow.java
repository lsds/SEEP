package uk.ac.imperial.lsds.seep.multi.tmp;

import uk.ac.imperial.lsds.seep.multi.ResultHandler;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;


public class TheCurrentWindow {
	
	private static enum CombinerType { MAX, MIN, CNT, SUM, AVG };
	
	/* Note that the following value must be a power of two (see `hash`). */
	private static final int CONTENT_SIZE = 1024;
	private static final int PANESET_SIZE = 1024;
	
	private static final int pid = 2 * Runtime.getRuntime().availableProcessors();
	IntermediateTuple [] contents;
	
	PaneSet heap;
	
	int size = 0;
	
	WindowDefinition windowDefinition;
	
	CombinerType combinerType = CombinerType.SUM;
	
	long windowIndex;
	
	long firstPaneIndex;
	long lastPaneIndex;
	
	long currPane = -1;
	
	public int size() {
		
		return this.size;
	}
	
	public boolean isEmpty () {
		
		return (this.size == 0);
	}
	
	public TheCurrentWindow (WindowDefinition windowDefinition) {
		
		contents = new IntermediateTuple [CONTENT_SIZE];
		
		for (int i = 0; i < contents.length; i++)
			contents[i] = null;
		
		heap = new PaneSet (PANESET_SIZE);
		
		size = 0;
		
		this.windowDefinition = windowDefinition;
		windowIndex = 0;
		
		firstPaneIndex = 0;
		lastPaneIndex = windowDefinition.numberOfPanes() - 1;
	}
	
	private void put (long timestamp, Key key, float value, int count) {
		
		IntermediateTuple current = contents[hash(key)];
		
		if (current == null) {
			
			contents[hash(key)] = 
				IntermediateTupleFactory.newInstance(pid, timestamp, key, value, count, null);
			size++;
		
		} else {
			
			while ((! current.key.eq(key)) && current.next != null)
				current = current.next;
			
			if (current.key.eq(key)) {
				
				/* Replace `value` and `count` */
				current.value = value;
				current.count = count;
			
			} else {
				
				current.next = IntermediateTupleFactory.newInstance(pid, timestamp, key, value, count, null);
				size++;
			}
		}
	}
	
	private boolean containsKey (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		if (current == null)
			return false;
		
		while ((! current.key.eq(key)) && current.next != null)
			current = current.next;
		
		if (current.key.eq(key))
			return true;
		
		return false;
	}
	
	private IntermediateTuple get (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		while ((! current.key.eq(key)) && current.next != null)
			current = current.next;
		
		if (! current.key.eq(key)) {
			System.err.println("error: key not found in pane");
			System.exit(1);
		}
		
		return current;
	}
	
	private int hash (Key key) {
		
		return key.hash() & (CONTENT_SIZE - 1);
	}
	
	public int clear() {
		
		size = 0;
		int count = 0;
		
		for (int i = 0; i < CONTENT_SIZE; i++) {
			
			if (contents[i] != null) {
				
				IntermediateTuple e = contents[i];
				while (e != null) {
					IntermediateTuple f = e.next;
					e.release(pid);
					count++;
					e = f;
				}
				contents[i] = null;
			}
		}
		return count;
	}
	
	private void remove (Key key) {
		
		IntermediateTuple current = contents[hash(key)];
		
		IntermediateTuple previous = null;
		
		while ((! current.key.eq(key)) && current.next != null) {
			previous = current;
			current = current.next;
		}
		
		if (current.key.eq(key)) {
			
			if (previous == null) {
				
				if (current.next != null)
					contents[hash(key)] = current.next;
				else 
					contents[hash(key)] = null;
			} else {
				if (current.next != null) 
					previous.next = current.next;
				else
					previous.next = null;
			}
			current.release(pid);
			size--;
		}
	}
	
	public IntermediateTuple [] getEntries() {
		
		return this.contents;
	}
	
	public void release () {
		
		heap.clear();
		clear();
	}
	
	public long getCurrPane() {
		return this.currPane;
	}
	
	public long getFirstPaneIndex() {
		return this.firstPaneIndex;
	}
	
	public long getLastPaneIndex() {
		return this.lastPaneIndex;
	}
	
	public long getWindowIndex() {
		return this.windowIndex;
	}
	
	/* Slide the window to the right */
	public void shiftRight (Pane pane) {
		
		currPane = pane.getPaneIndex();
		
		if (currPane < firstPaneIndex || currPane > lastPaneIndex) {
			System.err.println(String.format("error: pane %d not in window range (expected [%d, %d])", 
				currPane, firstPaneIndex, lastPaneIndex));
			System.exit(1);
		}
		
		/* Iterate over the items in `pane` and add them to the window */
		IntermediateTupleSet theSet = pane.getPaneSet();
		IntermediateTuple t, _t;
		Key key;
		for (int i = 1; i < theSet.next; i++) {
			t = theSet.getElement(i);
			/* Check if tuple exists in the table */
			if (containsKey(t.key)) {
				/* Update current entry in the table */
				_t = get(t.key);
				combine(_t, t);
			} else {
				/* Create a new entry in the table */
				/* Debug */
				// System.out.println("New key");
				key = KeyFactory.newInstance(t.key.type, pid);
				key.buffer.put(t.key.buffer);
				put(t.timestamp, key, t.value, t.count);
			}
		}
		
		/* Add pane to the `working pane set` */
		heap.add(pane);
		
		/* We cannot close the current window at this point,
		 * even if `currPane == lastPaneIndex`, since we do
		 * not know if `pane` is complete.
		 */
	}
	
	public void closeAndShiftLeft (ResultHandler handler) {
		
		/* Materialise intermediate tuple to a result stream */
		
		// TODO
		
		/* Compute new window pointers */
		long nextWindowStartsAt = firstPaneIndex + windowDefinition.panesPerSlide();
		
		/* Shift left:
		 * 
		 * Remove panes that no longer belong to the current window
		 * 
		 */
		long paneIdx = heap.tryFirst();
		Pane p;
		IntermediateTupleSet theSet;
		IntermediateTuple t, _t;
		Key key;
		while (paneIdx >= 0 && paneIdx < nextWindowStartsAt) {
			
			p = heap.remove();
			/* Iterate over the items in `pane` and remove them from the window */
			theSet = p.getPaneSet();
			for (int i = 1; i < theSet.next; i++) {
				t = theSet.getElement(i);
				/* Check if tuple exists in the table */
				if (! containsKey(t.key)) {
					System.err.println("error: pane element does not exist in window");
					System.exit(1);
				}
				/* Update current entry in the table */
				_t = get(t.key);
				invert(_t, t);
				if (_t.count == 0) {
					/* Remove element from window */
					remove (t.key);
				}
			}
			
			/* Free input data based on `p`'s free pointer */
			
			/* System.out.println(String.format("[DBG] [TheCurrentWindow] free pane %2d free pointer %10d", 
					p.getPaneIndex(), p.getFreeIndex()));
			*/
			
			handler.freeBuffer.free (p.getFreeIndex());
			
			/* Free pane */
			p.release();
			
			paneIdx = heap.tryFirst();
		}
		
		/* Set new window pointers */
		firstPaneIndex = nextWindowStartsAt;
		lastPaneIndex += windowDefinition.panesPerSlide();
		
		windowIndex ++;
	}
	
	private void combine(IntermediateTuple _t, IntermediateTuple t) {
		switch (combinerType) {
		case SUM:
		case CNT:
		case AVG:
			_t.value += t.value;
			_t.count += t.count;
			break;
		case MIN:
		case MAX:
			throw new IllegalStateException ("error: min/max is not supported yet");
		default:
			throw new IllegalStateException ("error: unknown combiner type");
		}
	}
	
	private void invert(IntermediateTuple _t, IntermediateTuple t) {
		switch (combinerType) {
		case SUM:
		case CNT:
		case AVG:
			_t.value -= t.value;
			_t.count -= t.count;
			break;
		case MIN:
		case MAX:
			throw new IllegalStateException ("error: min/max is not supported yet");
		default:
			throw new IllegalStateException ("error: unknown combiner type");
		}
	}
	
	public String toString () {
		
		return String.format("[window pool-%02d id %010d %6d items starts at %3d ends at %3d] ", 
				pid, windowIndex, size, firstPaneIndex, lastPaneIndex);
	}
}
