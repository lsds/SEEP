package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapEntryFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	private static ConcurrentLinkedQueue<IntMapEntry> [] pool = 
		(ConcurrentLinkedQueue<IntMapEntry> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<IntMapEntry>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new IntMapEntry(-1, -1, null));
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static IntMapEntry newInstance (int pid, int key, int value, IntMapEntry next) {
		IntMapEntry e = pool[pid].poll();
		if (e == null) {
			e = new IntMapEntry(key, value, next);
			count.incrementAndGet();
		}
		e.set(key, value, next);
		return e;
	}
	
	public static void free (int pid, IntMapEntry e) {
		/* The pool is ever growing based on peek demand */
		pool[pid].offer (e);
	}
}
