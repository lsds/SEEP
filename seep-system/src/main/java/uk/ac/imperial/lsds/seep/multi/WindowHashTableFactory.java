package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public class WindowHashTableFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static long idx = 0;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ArrayDeque<WindowHashTable> [] pool = 
		(ArrayDeque<WindowHashTable> []) new ArrayDeque [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ArrayDeque<WindowHashTable>();
			int i = _pool_size;
			while (i-- > 0)
				pool[n].add (new WindowHashTable(n, idx++));
		}
		count = new AtomicLong(idx);
	}
	
	public static WindowHashTable newInstance (int pid) {
		WindowHashTable e = pool[pid].poll();
		if (e == null) {
			idx = count.incrementAndGet();
			e = new WindowHashTable(pid, idx);
		}
		e.setId(pid);
		/* System.out.println(String.format("[DBG] WindowMap instance %s (count %4d)", e, count.get())); */
		return e;
	}
	
	public static void free (WindowHashTable e) {
		
		 pool[e.getId()].offer (e);
	}
}
