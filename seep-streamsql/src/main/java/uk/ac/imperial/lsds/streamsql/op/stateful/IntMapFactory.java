package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static LinkedList<IntMap> [] pool = 
		(LinkedList<IntMap> []) new LinkedList [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new LinkedList<IntMap>();
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
		// System.out.println(String.format("[DBG] get  IntMap instance (pid=%04d) (count %4d) (auto %4d)", e.getId(), count.get(), e.getAutoIndex()));
		return e;
	}
	
	public static void free (IntMap e) {
		/* The pool is ever growing based on peek demand */
		// System.out.println(String.format("[DBG] free IntMap instance (pid=%04d)", e.getId()));
		pool[e.getId()].offer (e);
	}
}
