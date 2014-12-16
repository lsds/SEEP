package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskProcessor implements Runnable {

	ConcurrentLinkedQueue<ITask>	queue;

	public TaskProcessor(ConcurrentLinkedQueue<ITask> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		ITask task = null;
		while (true) {
			try {

				while ((task = queue.poll()) == null) {
					/* LockSupport.parkNanos(1L); */
					Thread.yield();
				}
				task.run();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (task != null)
					task.free();
			}
		}
	}
}
