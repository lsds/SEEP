package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class TaskProcessor implements Runnable {

	TaskQueue queue;
	private int [][] policy;
	private int pid;
	boolean GPU;
	
	private int cid = 0; /* Processor class: GPU (0) or CPU (1) */
	
	/* Measurements */
	private AtomicLong [] tasksProcessed;
	
	public TaskProcessor (int pid, TaskQueue queue, int [][] policy, boolean GPU) {
		
		this.pid = pid;
		this.queue = queue;
		this.policy = policy;
		this.GPU = GPU;
		
		if (GPU) this.cid = 0;
		else 
			this.cid = 1;
		
		int nqueries = policy[0].length;
		this.tasksProcessed = new AtomicLong [nqueries];
		
		for (int i = 0; i < nqueries; i++)
			this.tasksProcessed[i] = new AtomicLong (0L);
	}

	@Override
	public void run() {
		Task task = null;
		if (GPU) {
			System.out.println ("[DBG] GPU thread is " + Thread.currentThread());
			/* TheGPU.getInstance().bind(1); */
		} else {
			/* TheGPU.getInstance().bind(pid + 1); */
		}
		
		while (true) {
			try {
				while ((task = queue.poll(policy, cid, 0)) == null) {
					LockSupport.parkNanos(1L);
				}
				
				/* Testing Java pass-by-value policy table
				 * 
				 * if (task.taskid % 100 == 0) {
					StringBuilder b = new StringBuilder(String.format("[DBG] p%02d [", pid));
					for (int i = 0; i < policy.length; i++) {
						for (int j = 0; j < policy[0].length; j++) {
							if (i == policy.length && j == policy[0].length)
								b.append(String.format("[%d][%d]=%4d",  i, j, policy[i][j]));
							else
								b.append(String.format("[%d][%d]=%4d ", i, j, policy[i][j]));
						}
					}
					b.append("]");
					System.out.println(b);
				} */
				
				task.setGPU(GPU);
				tasksProcessed[task.queryid].incrementAndGet();
				task.run();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				if (task != null) {
					/* System.out.println(String.format("[DBG] p %d (%s) frees task %6d query %d", 
					 * pid, GPU, ((Task) task).taskid, ((Task) task).queryid)); */
					task.free();
				}
			}
		}
	}
	
	public long getProcessedTasks(int qid) {
		return tasksProcessed[qid].get();
	}
}
