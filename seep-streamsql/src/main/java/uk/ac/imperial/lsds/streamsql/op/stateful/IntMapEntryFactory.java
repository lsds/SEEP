package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapEntryFactory {
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	private static ConcurrentLinkedQueue<IntMapEntry> pool = 
		new ConcurrentLinkedQueue<IntMapEntry>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new IntMapEntry(-1, -1, null));
		count = new AtomicLong(_pool_size);
	}
	
	public static IntMapEntry newInstance (int key, int value, IntMapEntry next) {
		IntMapEntry e = pool.poll();
		if (e == null) {
			e = new IntMapEntry(key, value, next);
			count.incrementAndGet();
		}
		e.set(key, value, next);
		return e;
	}
	
	public static void free (IntMapEntry e) {
		/* The pool is ever growing based on peek demand */
		pool.offer (e);
	}
}
