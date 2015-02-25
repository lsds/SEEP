package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ResultHandler {
	
	public int pad = 1;

	public final int SLOTS = 1024 * 1024;

	public IQueryBuffer freeBuffer;
	
	/*
	 * Flags:
	 *  -1 - slot is free
	 *   0 - slot is being populated by a thread
	 *   1 - slot is occupied, but "unlocked"
	 *   2 - slot is occupied, but "locked" (somebody is working on it)
	 */
	public PaddedAtomicInteger [] slots = new PaddedAtomicInteger [SLOTS];

	/*
	 * Structures to hold the actual data
	 */
	public IQueryBuffer [] results = new IQueryBuffer [SLOTS];
	public PaddedInteger [] offsets = new PaddedInteger [SLOTS];
	
	Semaphore semaphore; /* Protects next */
	PaddedInteger next;


	public ResultHandler (IQueryBuffer freeBuffer) {
		
		this.freeBuffer = freeBuffer;
		
		// slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			// slots.set(i, -1);
			slots[i] = new PaddedAtomicInteger(-1);
			offsets[i] = new PaddedInteger(Integer.MIN_VALUE);
		}
		
		next = new PaddedInteger(0);
		semaphore = new Semaphore(1, false);
	}
}
