package uk.ac.imperial.lsds.seep.multi;

/* import java.util.concurrent.ConcurrentLinkedQueue; */

public class TaskProcessor implements Runnable {

	/* ConcurrentLinkedQueue<ITask> queue; */
	TaskQueue queue;
	private int [][] policy;
	private int pid;
	boolean GPU;
	
	/*
	public TaskProcessor(ConcurrentLinkedQueue<ITask> queue, boolean GPU) {
		this.queue = queue;
		this.GPU = GPU;
	}
	*/
	public TaskProcessor(int pid, TaskQueue queue, int [][] policy, boolean GPU) {
		this.pid = pid;
		this.queue = queue;
		this.policy = policy;
		this.GPU = GPU;
	}

	@Override
	public void run() {
		ITask task = null;
		if (GPU)
			System.out.println ("GPU thread is " + Thread.currentThread());
		while (true) {
			try {

				/* while ((task = queue.poll()) == null) { */
				
				while ((task = queue.poll(policy, pid, 0)) == null) {
					/* LockSupport.parkNanos(1L); */
					Thread.yield();
				}
				task.setGPU(GPU);
				// System.out.println(String.format("[DBG] p %2d (%5s) runs task %6d from query %d", pid, GPU, ((Task) task).taskid, ((Task) task).queryid));
				task.run();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				if (task != null) {
					// System.out.println(String.format("[DBG] p %d (%s) frees task %6d query %d", pid, GPU, ((Task) task).taskid, ((Task) task).queryid));
					task.free();
				}
			}
		}
	}
}
