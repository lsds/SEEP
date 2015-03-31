package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/* import java.util.concurrent.ConcurrentLinkedQueue; */

public class TaskProcessor implements Runnable {

	/* ConcurrentLinkedQueue<ITask> queue; */
	TaskQueue queue;
	private int [][] policy;
	private int pid;
	boolean GPU;
	
	private int sel_pid = 0;
	
	/* Measurements */
	private AtomicLong tasksProcessed;
	
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
		
		if (GPU)
			this.sel_pid = 0;
		else
			this.sel_pid = 1;
		
		this.tasksProcessed = new AtomicLong (0L);
	}

	@Override
	public void run() {
		ITask task = null;
		if (GPU) {
			System.out.println ("GPU thread is " + Thread.currentThread());
//			TheGPU.getInstance().bind(1);
		} else {
			
			// sel_pid = 1;
			TheGPU.getInstance().bind(pid + 1);
		}
		// TheGPU.getInstance().bind(pid + 8);
		while (true) {
			try {
				
				/* while ((task = queue.poll()) == null) { */
				
				while ((task = queue.poll(policy, sel_pid, 0)) == null) {
//					System.err.println(String.format("warning: thread %20s blocked waiting for a task", 
//							Thread.currentThread()));
					LockSupport.parkNanos(1L);
					//Thread.yield();
					// ;
				}
				
				task.setGPU(GPU);
				// task.setBufferFactory(bufferFactory);
				// System.out.println(String.format("[DBG] p %2d (%d) (%5s) runs task %6d from query %d", pid, sel_pid, GPU, ((Task) task).taskid, ((Task) task).queryid));
				
//				if (this.pid > 4) {
//					Thread.sleep(5);
//				}
				
				tasksProcessed.incrementAndGet();
				
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

	public long getProcessedTasks() {
		return tasksProcessed.get();
	}
}
