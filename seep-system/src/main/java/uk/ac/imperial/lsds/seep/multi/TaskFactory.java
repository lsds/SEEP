package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import uk.ac.imperial.lsds.seep.multi.Task;

public class TaskFactory {
	
	private static int _pool_size = Utils.TASKS;
	
	private static ConcurrentLinkedQueue<Task> pool = 
		new ConcurrentLinkedQueue<Task>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new Task());
	}
	
	public static Task newInstance (SubQuery query, WindowBatch batch, ResultHandler handler, int taskid, int offset) {
		Task task; // = pool.poll();
		
		while ((task = pool.poll()) == null) {
//			System.err.println(String.format("warning: thread %20s blocked q %d t %4d waiting for a task", 
//					 Thread.currentThread(), query.getId(), taskid));
			LockSupport.parkNanos(1L);
			// ;
		}
		// if (task == null)
		//	return new Task(query, batch, handler, taskid, offset, query.getId());
		task.set(query, batch, handler, taskid, offset);
		return task;
	}
	
	public static void free (Task task) {
		/* The pool is ever growing based on peek demand */
		pool.offer (task);
	}
}
