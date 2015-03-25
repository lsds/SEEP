package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class WindowBatchFactory {
	
	private static final int _pool_size = 0; // Utils.TASKS; /* Initial pool size */
	
	public static AtomicLong count;
	
	private static ConcurrentLinkedQueue<WindowBatch> pool = 
		new ConcurrentLinkedQueue<WindowBatch>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new WindowBatch());
		count = new AtomicLong(_pool_size);
	}
	
	public static WindowBatch newInstance (int size, int task, int freeOffset, IQueryBuffer buffer, WindowDefinition window, ITupleSchema schema) {
		WindowBatch batch;
		
//		while ((batch = pool.poll()) == null) {
////			  System.err.println(String.format("warning: thread %20s blocked at task %d waiting for a batch to become available in the pool", 
////			 		 Thread.currentThread(), task));
//			// LockSupport.parkNanos(1L);
//			LockSupport.parkNanos(1L);
//		}
		
		batch = pool.poll();
		if (batch == null) {
			count.incrementAndGet();
			return new WindowBatch(size, task, freeOffset, buffer, window, schema);
		}
		batch.set(size, task, freeOffset, buffer, window, schema);
		return batch;
	}
	
	public static void free (WindowBatch batch) {
		/* The pool is ever growing based on peek demand */
		
		batch.clear();
		
		pool.offer (batch);
	}
}
