package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntermediateMapEntryFactory {
	
	private static int N = 32; // Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<IntermediateMapEntry> [] pool = 
		(ConcurrentLinkedQueue<IntermediateMapEntry> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<IntermediateMapEntry>();
			int i = _pool_size;
			while (i-- > 0) {
				IntermediateMapEntry e = new IntermediateMapEntry(-1, -1, -1, null);
				pool[n].add (e);
			}
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static IntermediateMapEntry newInstance (int pid, int key, float value, int countVal, IntermediateMapEntry next) {
		IntermediateMapEntry e = pool[pid].poll();
		if (e == null) {
			e = new IntermediateMapEntry(key, value, countVal, next);
			count.incrementAndGet();
		}
		e.set(key, value, countVal, next);
		return e;
	}
	
	public static void free (int pid, IntermediateMapEntry e) {
		/* The pool is ever growing based on peek demand */
		pool[pid].offer (e);
	}
}
