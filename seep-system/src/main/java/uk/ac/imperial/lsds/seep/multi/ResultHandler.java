package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ResultHandler {

	public final int SLOTS = Utils.TASKS;

	public IQueryBuffer freeBuffer;
	
	/*
	 * Flags:
	 *  -1 - slot is free
	 *   0 - slot is being populated
	 *   1 - slot is occupied, but unlocked
	 *   2 - slot is occupied, but locked (somebody is working on it)
	 */
	public AtomicIntegerArray slots;

	/*
	 * Structures to hold the actual data
	 */
	public IQueryBuffer [] results = new IQueryBuffer [SLOTS];
	public int [] offsets = new int[SLOTS];
	
	public AtomicMarkableReference<Integer> next;

	public ResultHandler (IQueryBuffer freeBuffer) {
		
		this.freeBuffer = freeBuffer;
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			offsets[i] = Integer.MIN_VALUE;
		}
		
		next = new AtomicMarkableReference<Integer>(new Integer(0), false);
	}
}
