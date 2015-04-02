package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntMapFactory {
	
	private static int _pool_size = 1;
	
	private static int idx = 0;
	
	public static AtomicLong count;
	
	private static ConcurrentLinkedQueue<IntMap> pool = 
		new ConcurrentLinkedQueue<IntMap>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new IntMap(idx++));
		count = new AtomicLong(_pool_size);
	}
	
	public static IntMap newInstance () {
		IntMap e = pool.poll();
		if (e == null) {
			e = new IntMap(idx++);
			count.incrementAndGet();
		}
		// System.out.println(String.format("[DBG] getting IntMap instance %04d", e.getId()));
		return e;
	}
	
	public static void free (IntMap e) {
		/* The pool is ever growing based on peek demand */
		// System.out.println(String.format("[DBG] freeing IntMap instance %04d", e.getId()));
		pool.offer (e);
	}
}
