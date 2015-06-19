package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public class WindowTupleFactory {
	
	private static int N = Runtime.getRuntime().availableProcessors();
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ArrayDeque<WindowTuple> [] pool = 
		(ArrayDeque<WindowTuple> []) new ArrayDeque [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ArrayDeque<WindowTuple>();
			int i = _pool_size;
			while (i-- > 0) {
				WindowTuple e = new WindowTuple ();
				pool[n].add (e);
			}
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static WindowTuple newInstance (int pid, int hashcode, IQueryBuffer buffer, int offset, int length, float value, int _count, WindowTuple next) {
		WindowTuple e = pool[pid].poll();
		if (e == null) {
			e = new WindowTuple (hashcode, buffer, offset, length, value, _count, next);
			count.incrementAndGet();
		}
		e.set (hashcode, buffer, offset, length, value, _count, next);
		return e;
	}
	
	public static void free (int pid, WindowTuple e) {
		pool[pid].offer (e);
	}
}
