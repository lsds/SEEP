package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class TaskProcessorPool {

	private int								workers;
	private ConcurrentLinkedQueue<ITask>	queue;
	private TaskProcessor[]					processor;

	public TaskProcessorPool(int workers,
			final ConcurrentLinkedQueue<ITask> queue) {
		this.workers = workers;
		this.queue = queue;
		System.out.println(String.format("[DBG] %d threads", this.workers));
		this.processor = new TaskProcessor[workers];
		for (int i = 0; i < workers; i++)
			this.processor[i] = new TaskProcessor(queue);
	}

	public ConcurrentLinkedQueue<ITask> start(Executor executor) {
		for (int i = 0; i < workers; i++)
			executor.execute(this.processor[i]);
		return queue;
	}
}
