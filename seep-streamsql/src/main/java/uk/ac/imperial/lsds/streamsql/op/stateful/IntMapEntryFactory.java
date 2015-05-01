package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapEntryFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static LinkedList<IntMapEntry> [] pool = 
		(LinkedList<IntMapEntry> []) new LinkedList [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new LinkedList<IntMapEntry>();
			int i = _pool_size;
			while (i-- > 0) {
				IntMapEntry e = new IntMapEntry(-1, -1, null);
				e.setAutoIndex(idx++);
				pool[n].add (e);
			}
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static IntMapEntry newInstance (int pid, int key, int value, IntMapEntry next) {
		IntMapEntry e = pool[pid].poll();
		if (e == null) {
			e = new IntMapEntry(key, value, next);
			// e.setAutoIndex(idx++);
			count.incrementAndGet();
		}
		e.set(key, value, next);
		// System.out.println(String.format("[DBG] get  IntMapEntry instance (pid=%04d) (count %4d) (auto %4d)", pid, count.get(), e.getAutoIndex()));
		return e;
	}
	
	public static void free (int pid, IntMapEntry e) {
		/* The pool is ever growing based on peek demand */
		pool[pid].offer (e);
	}
}
