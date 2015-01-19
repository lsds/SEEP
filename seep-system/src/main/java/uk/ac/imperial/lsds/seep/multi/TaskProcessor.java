package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskProcessor implements Runnable {

	ConcurrentLinkedQueue<ITask> queue;
	boolean GPU;

	public TaskProcessor(ConcurrentLinkedQueue<ITask> queue, boolean GPU) {
		this.queue = queue;
		this.GPU = GPU;
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
				task.setGPU(GPU);
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
