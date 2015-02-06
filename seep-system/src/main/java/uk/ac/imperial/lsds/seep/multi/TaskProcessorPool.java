package uk.ac.imperial.lsds.seep.multi;

/* import java.util.concurrent.ConcurrentLinkedQueue; */
import java.util.concurrent.Executor;

public class TaskProcessorPool {

	private int workers;
	/* private ConcurrentLinkedQueue<ITask>	queue; */
	private TaskQueue queue;
	private TaskProcessor[] processor;
	private int [][] policy;
	
	/*
	public TaskProcessorPool(int workers,
			final ConcurrentLinkedQueue<ITask> queue, boolean hybrid) {
		this.workers = workers;
		this.queue = queue;
		System.out.println(String.format("[DBG] %d threads", this.workers));
		this.processor = new TaskProcessor[workers];
		if (hybrid) {
			// Assign the first processor to be the GPU manager
			this.processor[0] = new TaskProcessor(queue, true);
			for (int i = 1; i < workers; i++)
				this.processor[i] = new TaskProcessor(queue, false);
		} else {
			for (int i = 0; i < workers; i++)
				this.processor[i] = new TaskProcessor(queue, false);
		}
	}
	*/
	
	public TaskProcessorPool(int workers,
			final TaskQueue queue, int [][] policy, boolean hybrid) {
		this.workers = workers;
		this.queue = queue;
		this.policy = policy;
		System.out.println(String.format("[DBG] %d threads", this.workers));
		this.processor = new TaskProcessor[workers];
		if (hybrid) {
			// Assign the first processor to be the GPU manager
			this.processor[0] = new TaskProcessor(0, queue, policy, true);
			for (int i = 1; i < workers; i++)
				this.processor[i] = new TaskProcessor(i, queue, policy, false);
		} else {
			for (int i = 0; i < workers; i++)
				this.processor[i] = new TaskProcessor(i, queue, policy, false);
		}
	}
	
	/*
	public ConcurrentLinkedQueue<ITask> start(Executor executor) {
		for (int i = 0; i < workers; i++)
			executor.execute(this.processor[i]);
		return queue;
	}
	*/
	
	public TaskQueue start(Executor executor) {
		for (int i = 0; i < workers; i++)
			executor.execute(this.processor[i]);
		return queue;
	}
}
