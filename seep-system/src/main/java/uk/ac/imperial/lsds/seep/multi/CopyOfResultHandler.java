package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class CopyOfResultHandler {
	
	public int pad = 1;

	public final int SLOTS = 1024 * 1024 * pad; // Utils.TASKS * pad;

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
	
	Semaphore semaphore; /* Protects next */
	int next;
//	int p1_1 = 0, p2_1 = 0, p3_1 = 0, p4_1 = 0, p5_1 = 0, p6_1 = 0, p7_1 = 0, p8_1 = 0, p9_1 = 0, p10_1 = 0, 
//	 		p11_1 = 0, p12_1 = 0, p13_1 = 0, p14_1 = 0, p15_1 = 0; 
//	int p1_2 = 0, p2_2 = 0, p3_2 = 0, p4_2 = 0, p5_2 = 0, p6_2 = 0, p7_2 = 0, p8_2 = 0, p9_2 = 0, p10_2 = 0, 
//	 		p11_2 = 0, p12_2 = 0, p13_2 = 0, p14_2 = 0, p15_2 = 0; 

	public CopyOfResultHandler (IQueryBuffer freeBuffer) {
		
		this.freeBuffer = freeBuffer;
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			offsets[i] = Integer.MIN_VALUE;
		}
		
		next = 0;
		semaphore = new Semaphore(1, false);
	}
}
