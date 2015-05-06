package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapEntryFactory {
	
	private static int N = 32; // Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ArrayDeque<IntMapEntry> [] pool = 
		(ArrayDeque<IntMapEntry> []) new ArrayDeque [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ArrayDeque<IntMapEntry>();
			int i = _pool_size;
			while (i-- > 0) {
				IntMapEntry e = new IntMapEntry(-1, -1, null);
				pool[n].add (e);
			}
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
