package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

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
	
	public static WindowBatch newInstance (int size, int task, int freeOffset, IQueryBuffer buffer, 
			WindowDefinition window, ITupleSchema schema, int latencyMark) {
		WindowBatch batch;
		batch = pool.poll();
		if (batch == null) {
			count.incrementAndGet();
			return new WindowBatch(size, task, freeOffset, buffer, window, schema, latencyMark);
		}
		batch.set(size, task, freeOffset, buffer, window, schema, latencyMark);
		return batch;
	}
	
	public static void free (WindowBatch batch) {
		/* The pool is ever growing based on peek demand */
		
		batch.clear();
		
		pool.offer (batch);
	}
}
