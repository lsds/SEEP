package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ResultHandler {

	public final int SLOTS = Utils.TASKS;

	public IQueryBuffer freeBuffer;
	
	public int next;
	/*
	 * Flags:
	 *  0 - slot is FREE
	 *  1 - slot is full, but nobody took it
	 *  2 - slot is full, but somebody is working on it (do not touch!)
	 */
	public AtomicIntegerArray slots;

	/*
	 * Structures to hold the actual data
	 */
	public IQueryBuffer[] results = new IQueryBuffer[SLOTS];
	public int[] offsets = new int[SLOTS]; 
	
	public AtomicInteger lock, counter;

	public ResultHandler(IQueryBuffer freeBuffer) {
		this.freeBuffer = freeBuffer;
		slots = new AtomicIntegerArray(SLOTS);
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, 0);
			offsets[i] = Integer.MIN_VALUE;
		}
		
//		offsets = new AtomicIntegerArray(SLOTS);
		next = 0;
//		lock = new AtomicInteger(0);
//		counter = new AtomicInteger(0);
//		for (int i = 0; i < SLOTS; i++) {
//			slots.set(i, 1);
//			offsets.set(i, -1);
//		}
	}
}
