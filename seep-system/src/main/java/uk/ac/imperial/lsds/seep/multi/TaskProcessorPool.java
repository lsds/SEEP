package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Executor;

public class TaskProcessorPool {

	private int workers;
	
	private TaskQueue queue;
	private TaskProcessor [] processor;
	private int [][] policy;
	
	public TaskProcessorPool (int workers, final TaskQueue queue, int [][] policy, boolean hybrid) {
		
		this.workers = workers;
		this.queue = queue;
		this.policy = policy;
		
		System.out.println(String.format("[DBG] %d threads (hybrid mode %s)", this.workers, hybrid));
		
		this.processor = new TaskProcessor[workers];
		if (hybrid) {
			/* Assign the first processor to be the GPU worker */
			this.processor[0] = new TaskProcessor(0, queue, policy, true);
			for (int i = 1; i < workers; i++)
				this.processor[i] = new TaskProcessor(i, queue, policy, false);
		} else {
			for (int i = 0; i < workers; i++)
				this.processor[i] = new TaskProcessor(i, queue, policy, false);
		}
		
		/* Enable monitoring */
		for (int i = 0; i < workers; i++)
			this.processor[i].enableMonitoring();
	}
	
	public TaskQueue start(Executor executor) {
		for (int i = 0; i < workers; i++)
			executor.execute(this.processor[i]);
		return queue;
	}
	
	public long getProcessedTasks (int pid, int qid) {
		return processor[pid].getProcessedTasks(qid);
	}
	
	public double mean (int pid) {
		return processor[pid].mean();
	}
	
	public double stdv (int pid) {
		return processor[pid].stdv();
	}
}
