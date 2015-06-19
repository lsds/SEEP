package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public class WindowMapFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ArrayDeque<WindowMap> [] pool = 
		(ArrayDeque<WindowMap> []) new ArrayDeque [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ArrayDeque<WindowMap>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new WindowMap(n, idx++));
		}
		count = new AtomicLong(idx);
	}
	
	public static WindowMap newInstance (int pid) {
		WindowMap e = pool[pid].poll();
		if (e == null) {
			idx = count.incrementAndGet();
			e = new WindowMap(pid, idx);
		}
		e.setId(pid);
		/* System.out.println(String.format("[DBG] WindowMap instance %s (count %4d)", e, count.get())); */
		return e;
	}
	
	public static void free (WindowMap e) {
		
		 pool[e.getId()].offer (e);
	}
}
