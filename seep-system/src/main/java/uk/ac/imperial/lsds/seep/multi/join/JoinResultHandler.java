package uk.ac.imperial.lsds.seep.multi.join;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.Utils;

public class JoinResultHandler {

	public final int SLOTS = Utils.TASKS * 4;
	
	public IQueryBuffer  firstFreeBuffer;
	public IQueryBuffer secondFreeBuffer;
	
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
	
	public int []  firstOffsets = new int[SLOTS];
	public int [] secondOffsets = new int[SLOTS];
	
	/* A query can have more than one downstream sub-queries. */
	public int [] latch = new int [SLOTS];
	
	public Semaphore semaphore; /* Protects next */
	public int next;

	public int wraps = 0;
	
	public JoinResultHandler(IQueryBuffer firstFreeBuffer, IQueryBuffer secondFreeBuffer) {
		
		this.firstFreeBuffer  =  firstFreeBuffer;
		this.secondFreeBuffer = secondFreeBuffer;
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			 firstOffsets[i] = Integer.MIN_VALUE;
			secondOffsets[i] = Integer.MIN_VALUE;
			
			latch[i] = 0;
		}
		
		next = 0;
		semaphore = new Semaphore(1, false);
	}
}
