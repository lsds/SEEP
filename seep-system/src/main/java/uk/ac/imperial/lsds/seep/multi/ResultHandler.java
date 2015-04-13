package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

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
	
	Semaphore semaphore; /* Protects next */
	int next;

	public int wraps = 0;

	public ResultHandler (IQueryBuffer freeBuffer, SubQuery query) {
		
		this.freeBuffer = freeBuffer;
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			offsets[i] = Integer.MIN_VALUE;
			
			latch[i] = 0;
		}
		
		next = 0;
		semaphore = new Semaphore(1, false);
	}
}
