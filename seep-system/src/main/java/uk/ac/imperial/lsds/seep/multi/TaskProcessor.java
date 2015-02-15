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
				// System.out.println(String.format("[DBG] p %d (%s) runs task %d", pid, GPU, ((Task) task).taskid));
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
