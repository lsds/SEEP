package uk.ac.imperial.lsds.seep.multi;

public class IntermediateTupleSet {
	
	private static class IntermediateTupleSetNode {
		
		int key;
		IntermediateTuple tuple;
		
		public IntermediateTupleSetNode () {
			
			init (Integer.MIN_VALUE, null);
		}
		
		public void init (int key, IntermediateTuple tuple) {
			
			this.key = key;
			this.tuple = tuple;
		}
	}
	
	private static final int root = 1;
	
	int next;
	IntermediateTupleSetNode [] heap;
	
	public IntermediateTupleSet (int capacity) {
		next = root;
		heap = new IntermediateTupleSetNode [capacity + 1];
		for (int i = 0; i < capacity + 1; i++)
			heap[i] = new IntermediateTupleSetNode ();
	}
	
	private void swap (IntermediateTupleSetNode p, IntermediateTupleSetNode q) {
		int k = p.key;
		IntermediateTuple t = p.tuple;
		p.key = q.key;
		p.tuple = q.tuple;
		q.key = k;
		q.tuple = t;
	}
	
	public void add (int key, IntermediateTuple tuple) {
		int child = next++;
		heap[child].init(key, tuple);
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
	
	public IntermediateTuple remove () {
		
		if (next <= root)
			return null;
		
		int bottom = --next;
		IntermediateTuple tuple = heap[root].tuple;
		heap[root] = heap[bottom];
		if (bottom == root)
			return tuple;
		
		int child = 0;
		int parent = root;
		while (parent < heap.length / 2) {
			int left = parent * 2;
			int right = left + 1;
			if (left >= next)
				return tuple;
			else if (right >= next || heap[left].key < heap[right].key)
				child = left;
			else
				child = right;
			if (heap[child].key < heap[parent].key) {
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
}
