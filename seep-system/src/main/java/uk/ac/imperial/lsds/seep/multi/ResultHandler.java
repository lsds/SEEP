package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ResultHandler {

	public final int SLOTS = Utils.TASKS; // + 1;

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
	public int [] offsets = new int[SLOTS];
	
	Semaphore semaphore; /* Protects next */
	int next;

	public int wraps = 0;
	
	Pusher pusher;

	public ResultHandler (IQueryBuffer freeBuffer, SubQuery query) {
		
		this.freeBuffer = freeBuffer;
		
		slots = new AtomicIntegerArray(SLOTS);
		
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, -1);
			offsets[i] = Integer.MIN_VALUE;
		}
		
		next = 0;
		semaphore = new Semaphore(1, false);
		
//		pusher = new Pusher(this, query);
//		
//		Thread pusherThr = new Thread(pusher);
//		pusherThr.setName("Pusher-" + query.getId());
//		pusherThr.start();
	}

	public void stopPushing() {
		((Pusher) pusher).stopWorking();
	}
}
