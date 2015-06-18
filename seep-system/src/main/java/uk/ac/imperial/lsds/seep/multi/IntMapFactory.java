package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 100;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ArrayDeque<IntMap> [] pool = 
		(ArrayDeque<IntMap> []) new ArrayDeque [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ArrayDeque<IntMap>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new IntMap(n, idx++));
		}
		count = new AtomicLong(idx);
	}
	
	public static IntMap newInstance (int pid) {
		IntMap e = pool[pid].poll();
		if (e == null) {
			idx = count.incrementAndGet();
			e = new IntMap(pid, idx);
		}
		e.setId(pid);
		/* System.out.println(String.format("[DBG] IntMap instance %s (count %4d)", e, count.get())); */
		return e;
	}
	
	public static void free (IntMap e) {
		/* The pool is ever growing based on peek demand */
		
		/*
		 * Do not return IntMap; just allocate new ones.
		 */ 
		 pool[e.getId()].offer (e);
		 /**/
	}
}
