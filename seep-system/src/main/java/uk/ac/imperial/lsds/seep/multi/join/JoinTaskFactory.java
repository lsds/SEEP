package uk.ac.imperial.lsds.seep.multi.join;

import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;

public class JoinTaskFactory {

	private static int								_pool_size	= Utils.TASKS;

	private static ConcurrentLinkedQueue<JoinTask>	pool		= new ConcurrentLinkedQueue<JoinTask>();

	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add(new JoinTask());
	}

	public static JoinTask newInstance(SubQuery query, WindowBatch firstBatch,
			WindowBatch secondBatch, JoinResultHandler handler, int taskid,
			int firstOffset, int secondOffset) {
		JoinTask task = pool.poll();
		if (task == null)
			return new JoinTask(query, firstBatch, secondBatch, handler,
					taskid, firstOffset, secondOffset);
		task.set(query, firstBatch, secondBatch, handler, taskid, firstOffset,
				secondOffset);
		return task;
	}

	public static void free(JoinTask task) {
		/* The pool is ever growing based on peek demand */
		pool.offer(task);
	}
}
