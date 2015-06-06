package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class IntermediateTupleFactory {
	
	private static int N = 2 * Runtime.getRuntime().availableProcessors() + 1;
	
	private static int _pool_size = 1;
	
	public static AtomicLong count;
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<IntermediateTuple> [] pool = 
		(ConcurrentLinkedQueue<IntermediateTuple> []) new ConcurrentLinkedQueue [N];
	
	static {
		for (int n = 0; n < N; n++) {
			pool[n] = new ConcurrentLinkedQueue<IntermediateTuple>();
			int i = _pool_size;
			while (i-- > 0) {
				IntermediateTuple e = new IntermediateTuple ();
				pool[n].add (e);
			}
		}
		count = new AtomicLong(_pool_size * N);
	}
	
	public static IntermediateTuple newInstance 
		(int pid, long timestamp, Key key, float value, int counter, IntermediateTuple next) {
		
		IntermediateTuple e = pool[pid].poll();
		
		if (e == null) {
			
			e = new IntermediateTuple();
			count.incrementAndGet();
		}
		
		e.set(timestamp, key, value, counter, next);
		
		return e;
	}
	
	public static void free (int pid, IntermediateTuple e) {
		
		pool[pid].offer (e);
	}
}
