package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.concurrent.ConcurrentLinkedQueue;

public class IntMapFactory {
	
	private static int _pool_size = 1000;
	
	private static ConcurrentLinkedQueue<IntMap> pool = 
		new ConcurrentLinkedQueue<IntMap>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new IntMap());
	}
	
	public static IntMap newInstance () {
		IntMap e = pool.poll();
		if (e == null)
			return new IntMap();
		return e;
	}
	
	public static void free (IntMap e) {
		/* The pool is ever growing based on peek demand */
		pool.offer (e);
	}
}
