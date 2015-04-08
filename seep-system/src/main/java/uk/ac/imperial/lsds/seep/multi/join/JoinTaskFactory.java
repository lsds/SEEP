package uk.ac.imperial.lsds.seep.multi.join;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;

public class JoinTaskFactory {

	private static int _pool_size = 1;
	
	public static AtomicLong count;

	private static ConcurrentLinkedQueue<JoinTask> pool = new ConcurrentLinkedQueue<JoinTask>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add(new JoinTask());
		count = new AtomicLong(_pool_size);
	}

	public static JoinTask newInstance(SubQuery query, WindowBatch batch1, WindowBatch batch2, JoinResultHandler handler, 
			int taskid, int offset1, int offset2) {
		
		JoinTask task = pool.poll();
		if (task == null) {
			count.incrementAndGet();
			return new JoinTask(query, batch1, batch2, handler, taskid, offset1, offset2, query.getId());	
		}
		task.set(query, batch1, batch2, handler, taskid, offset1, offset2);
		return task;
	}
	
	public static void free(JoinTask task) {
		/* The pool is ever growing based on peek demand */
		pool.offer(task);
	}
}
