package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntermediateMapFactory {
	
	private static int N = 32; // Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 100;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<IntermediateMap> [] pool = 
		(ConcurrentLinkedQueue<IntermediateMap> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<IntermediateMap>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new IntermediateMap(n, idx++));
		}
		count = new AtomicLong(idx);
	}
	
	public static IntermediateMap newInstance (int pid) {
		IntermediateMap e = pool[pid].poll();
		if (e == null) {
			idx = count.incrementAndGet();
			e = new IntermediateMap(pid, idx);
		}
		e.setId(pid);
		/* System.out.println(String.format("[DBG] IntMap instance %s (count %4d)", e, count.get())); */
		return e;
	}
	
	public static void free (IntermediateMap e) {
		/* The pool is ever growing based on peek demand */
		
		/*
		 * Do not return IntMap; just allocate new ones.
		 */ 
		 pool[e.getId()].offer (e);
		 /**/
	}
}
