package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	private static ConcurrentLinkedQueue<IntMap> [] pool = 
		(ConcurrentLinkedQueue<IntMap> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<IntMap>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new IntMap());
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static IntMap newInstance (int pid) {
		IntMap e = pool[pid].poll();
		if (e == null) {
			e = new IntMap();
			count.incrementAndGet();
		}
		e.setId(pid);
		/* System.out.println(String.format("[DBG] getting IntMap instance (pid=%04d)", e.getId())); */
		return e;
	}
	
	public static void free (IntMap e) {
		/* The pool is ever growing based on peek demand */
		// System.out.println(String.format("[DBG] freeing IntMap instance %04d", e.getId()));
		pool[e.getId()].offer (e);
	}
}
