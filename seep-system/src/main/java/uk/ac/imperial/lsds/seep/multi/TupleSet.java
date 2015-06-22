package uk.ac.imperial.lsds.seep.multi;

public class TupleSet {
	
	private static class TupleSetNode {
		
		WindowTuple tuple;
		
		public TupleSetNode () {
			
			this.tuple = null;
		}
		
		public void init (WindowTuple tuple) {
			
			this.tuple = tuple;
		}
		
		public String toString() {
			
			return String.format("[index %10d] [code %10d]", tuple.offset, this.hashCode());
		}

		public int compareTo (TupleSetNode otherNode) {
			
			return this.tuple.compareTo(otherNode.tuple.offset);
		}

		public void reset() {
			
			this.tuple = null;
		}
	}
	
	private static final int root = 1;
	
	public int next;
	public TupleSetNode [] heap;
	
	public TupleSet (int capacity) {
		next = root;
		heap = new TupleSetNode [capacity + 1];
		for (int i = 0; i < capacity + 1; i++)
			heap[i] = new TupleSetNode ();
	}
	
	private void swap (TupleSetNode x, TupleSetNode y) {
		
		WindowTuple p = x.tuple;
		x.tuple = y.tuple;
		y.tuple = p;
	}
	
	public void add (WindowTuple tuple) {
		int child = next++;
		heap[child].init(tuple);
		while (child > root) {
			int parent = child / 2;
			if (heap[child].compareTo(heap[parent]) < 0) {
				swap (heap[child], heap[parent]);
				child = parent;
			} else {
				return;
			}
		}
	}
	
	public WindowTuple remove () {
		
		if (next <= root)
			return null;
		
		int bottom = --next;
		WindowTuple tuple = heap[root].tuple;
		/* heap[root] = heap[bottom]; */
		swap(heap[root], heap[bottom]);
		if (bottom == root)
			return tuple;
		
		int child = 0;
		int parent = root;
		while (parent < heap.length / 2) {
			int left = parent * 2;
			int right = left + 1;
			if (left >= next)
				return tuple;
			else if (right >= next || (heap[left].compareTo(heap[right]) < 0))
				child = left;
			else
				child = right;
			if (heap[child].compareTo(heap[parent]) < 0) {
				swap(heap[child], heap[parent]);
				parent = child;
			} else
				return tuple;
		}
		return tuple;
	}
	
	public void clear () {
		while (remove() != null)
			;
	}
	
	public synchronized void dump () {
		for (int i = 1; i < next; i++) {
			System.out.println(String.format("[DBG] [TheCurrentWindow.PaneSet] [%04d] %s", i, heap[i]));
		}
	}

	public WindowTuple getTuple(int i) {
		return heap[i].tuple;
	}

	public void reset() {
		for (int i = 1; i < next; i++)
			heap[i].reset();
		next = root;
	}
}
