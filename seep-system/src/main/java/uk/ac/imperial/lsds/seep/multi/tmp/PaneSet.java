package uk.ac.imperial.lsds.seep.multi.tmp;

public class PaneSet {
	
	private static class PaneSetNode {
		
		long key;
		Pane pane;
		
		public PaneSetNode () {
			
			this.key = Long.MIN_VALUE;
			this.pane = null;
		}
		
		public void init (Pane pane) {
			
			this.key = pane.getPaneIndex();
			this.pane = pane;
		}
		
		public String toString() {
			
			return String.format("[index %10d] [code %10d]", key, this.hashCode());
		}
	}
	
	private static final int root = 1;
	
	int next;
	PaneSetNode [] heap;
	
	public PaneSet (int capacity) {
		next = root;
		heap = new PaneSetNode [capacity + 1];
		for (int i = 0; i < capacity + 1; i++)
			heap[i] = new PaneSetNode ();
	}
	
	private void swap (PaneSetNode x, PaneSetNode y) {
		long k = x.key;
		Pane p = x.pane;
		x.key  = y.key;
		x.pane = y.pane;
		y.key  = k;
		y.pane = p;
	}
	
	public void add (Pane pane) {
		int child = next++;
		heap[child].init(pane);
		while (child > root) {
			int parent = child / 2;
			if (heap[child].key < heap[parent].key) {
				swap (heap[child], heap[parent]);
				child = parent;
			} else {
				return;
			}
		}
	}
	
	public long tryFirst () {
		if (next <= 1)
			return -1;
		return heap[root].key;
	}
	
	public Pane remove () {
		
//		if (next <= root)
//			return null;
		
		int bottom = --next;
		Pane pane = heap[root].pane;
		/* heap[root] = heap[bottom]; */
		swap(heap[root], heap[bottom]);
		if (bottom == root)
			return pane;
		
		int child = 0;
		int parent = root;
		while (parent < heap.length / 2) {
			int left = parent * 2;
			int right = left + 1;
			if (left >= next)
				return pane;
			else if (right >= next || heap[left].key < heap[right].key)
				child = left;
			else
				child = right;
			if (heap[child].key < heap[parent].key) {
				swap(heap[child], heap[parent]);
				parent = child;
			} else
				return pane;
		}
		return pane;
	}
	
	public void clear () {
		while (remove() != null)
			;
	}
	
	public Pane getElement(int i) {
		return heap[i].pane;
	}
	
	public synchronized void dump () {
		for (int i = 1; i < next; i++) {
			System.out.println(String.format("[DBG] [TheCurrentWindow.PaneSet] [%04d] %s", i, heap[i]));
		}
	}
}
