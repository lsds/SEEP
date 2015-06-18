package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class PartialWindowResultsFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<PartialWindowResults> [] pool = 
		(ConcurrentLinkedQueue<PartialWindowResults> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<PartialWindowResults>();
			int i = _pool_size;
			while (i-- > 0) {
				PartialWindowResults e = new PartialWindowResults(n);
				pool[n].add (e);
			}
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static PartialWindowResults newInstance (int pid) {
		PartialWindowResults e = pool[pid].poll();
		if (e == null) {
			e = new PartialWindowResults(pid);
			count.incrementAndGet();
		}
		e.init();
		return e;
	}
	
	public static void free (int pid, PartialWindowResults e) {
		pool[pid].offer (e);
	}
}
