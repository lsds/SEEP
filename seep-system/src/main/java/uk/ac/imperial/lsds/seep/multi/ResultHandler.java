package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicStampedReference;

import uk.ac.imperial.lsds.seep.multi.tmp.ResultCollectorNode;

public class ResultHandler {
	
	public final int SLOTS = Utils.TASKS * 4;

	public IQueryBuffer freeBuffer;
	
	/*
	 * Flags:
	 *  -1 - slot is free
	 *   0 - slot is being populated by a thread
	 *   1 - slot is occupied, but "unlocked"
	 *   2 - slot is occupied, but "locked" (somebody is working on it)
	 */
	public AtomicIntegerArray slots;

	/*
	 * Structures to hold the actual data
	 */
	public IQueryBuffer [] results = new IQueryBuffer [SLOTS];
	public int [] offsets = new int [SLOTS];
	
	/* A query can have more than one downstream sub-queries. */
	public int [] latch = new int [SLOTS];
	
	public int [] mark  = new int [SLOTS];
	
	Semaphore semaphore; /* Protects next */
	int next;

	public int wraps = 0;
	
	private long totalOutputBytes = 0L;

	// public TheWindowHeap theWindowHeap;
	
	// public TheCurrentWindow theCurrentWindow;
	
	// public ConcurrentLinkedQueue<WindowResult> windowResults;
	
	// public WindowResultList windowResults;
	
	// public WindowResultHeap windowResults;
	
	public AtomicIntegerArray windowSlots;
	public IQueryBuffer [] windowResults = new IQueryBuffer [SLOTS];
	public int [] windowOffsets = new int [SLOTS];

	public int nextWindow;
	
	/* Plan D */
	
	public AtomicIntegerArray taskSlots;
	/* Leaf nodes that correspond to a task slot */
	public ResultCollectorNode [] leaves;
	/* Tree nodes; for every pair of leaves, there is one parent node */
	public ResultCollectorNode [] nodes;
	
	/* Plan E */
	ResultAggregator resultAggregator;
	
	public ResultHandler (IQueryBuffer freeBuffer, SubQuery query) {
		
		this.freeBuffer = freeBuffer;
		
		System.out.println(SLOTS + " slots");
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			offsets[i] = Integer.MIN_VALUE;
			
			latch[i] = 0;
			mark [i] =-1;
		}
		
		next = 0;
		semaphore = new Semaphore(1, false);
		
		nextWindow = 0;
		
		windowSlots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			windowSlots.set(i, -1);
			windowOffsets[i] = Integer.MIN_VALUE;
		}
		
		/* Plan D */
		
		taskSlots = new AtomicIntegerArray (SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			taskSlots.set(i, -1);
		}
		
		leaves = new ResultCollectorNode [SLOTS];
		
		for (int i = 0; i < SLOTS; i++) {
			 leaves [i] = new ResultCollectorNode ();
		}
		
		/* Create and connect tree nodes */
		nodes = new ResultCollectorNode [SLOTS];
		for (int i = 0; i < SLOTS; i++) {
			
			nodes[i] = new ResultCollectorNode ();
			/* This node is parent to i-right and (i + 1)-left */
			int curr = i;
			int next = i + 1;
			if (next >= SLOTS) 
				next = 0;
			leaves[curr].setRightParent(nodes[i]);
			leaves[next].setLeftParent (nodes[i]);
		}
		
		// theWindowHeap = new TheWindowHeap ();
		
		// theCurrentWindow = new TheCurrentWindow(query.getWindowDefinition());
		// System.out.println(theCurrentWindow);
		
		// windowResults = new ConcurrentLinkedQueue<WindowResult>(); // 
		
		// windowResults = new WindowResultList ();
		
		// windowResults = new WindowResultHeap ();
		
		
		resultAggregator = new ResultAggregator(SLOTS, freeBuffer, query);
	}
	
	public long getTotalOutputBytes () {
		
		return totalOutputBytes;
	}
	
	public void incTotalOutputBytes (int bytes) {
		
		totalOutputBytes += (long) bytes;
	}
}
