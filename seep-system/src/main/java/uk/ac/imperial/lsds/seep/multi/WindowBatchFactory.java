package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WindowBatchFactory {
	
	private static final int   _pool_size = Utils.TASKS; /* Initial pool size */
	
	private static ConcurrentLinkedQueue<WindowBatch> pool = 
		new ConcurrentLinkedQueue<WindowBatch>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new WindowBatch());
	}
	
	public static WindowBatch newInstance (int size, int task, int freeOffset, IQueryBuffer buffer, WindowDefinition window, ITupleSchema schema) {
		WindowBatch batch;
		
		while ((batch = pool.poll()) == null)
		 	;
		
//		batch = pool.poll();
//		if (batch == null)
//			return new WindowBatch(size, task, freeOffset, buffer, window, schema);
		batch.set(size, task, freeOffset, buffer, window, schema);
		return batch;
	}
	
	public static void free (WindowBatch batch) {
		/* The pool is ever growing based on peek demand */
		batch.clear();
		pool.offer (batch);
	}
}
