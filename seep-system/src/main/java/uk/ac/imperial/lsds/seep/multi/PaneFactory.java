package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.atomic.AtomicLong;

public class PaneFactory {
	
	private static int N = 2 * Runtime.getRuntime().availableProcessors() + 1;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<Pane> [] pool = 
		(ConcurrentLinkedQueue<Pane> []) new ConcurrentLinkedQueue [N];
	
	static {
		
		for (int n = 0; n < N; n++) {
			
			pool[n] = new ConcurrentLinkedQueue<Pane>();
			
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new Pane(n));
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static Pane newInstance (int pid) {
		
		Pane pane = pool[pid].poll();
		
		if (pane == null) {
			pane = new Pane (pid);
			count.incrementAndGet();
		}
		pane.setProcessorId (pid);
		
		return pane;
	}
	
	public static void free (Pane e) {
		
		pool[e.getProcessorId()].offer (e);
	}
}
