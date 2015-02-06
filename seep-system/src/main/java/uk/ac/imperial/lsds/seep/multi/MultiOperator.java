package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MultiOperator {

	private static int		threads	= Utils.THREADS;

	private Set<SubQuery>	subQueries;
	private int				id;

	private ITaskDispatcher	dispatcher;
	/* private ConcurrentLinkedQueue<ITask>	queue, _queue; */
	private TaskQueue queue;
	private TaskProcessorPool workerPool, _workerPool;
	private Executor executor, _executor;
	
	int [][] policy;

	public MultiOperator(Set<SubQuery> subQueries, int id) {
		this.subQueries = subQueries;
		this.id = id;
	}

	public void processData(byte[] values) {

		this.dispatcher.dispatch(values);
	}

	public void processDataSecond(byte[] values) {

		this.dispatcher.dispatchSecond(values);
	}

	public void setup() {

//		if (Utils.HYBRID) {
//			/*
//			 * Allocate one thread for the GPU executor service, and the
//			 * remaining threads to the CPU executor service.
//			 */
//			if (threads < 2) {
//				throw new IllegalArgumentException(
//						"error: insufficient number of threads for hybrid execution mode");
//			}
//			/*
//			 * Allocate one out of `threads` workers to the GPU.
//			 */
//			this._queue = new ConcurrentLinkedQueue<ITask>();
//			this._workerPool = new TaskProcessorPool(1, _queue);
//			this._executor = Executors.newCachedThreadPool();
//			this._queue = _workerPool.start(_executor);
//
//			threads--;
//		}
		
		this.policy = new int [threads][1];
		for (int i = 0; i < threads; i++) {
			policy [i][0] = 1;
		}
		// policy[1][0] = 8;

		this.queue = new TaskQueue(threads, 1); // new ConcurrentLinkedQueue<ITask>();
		this.workerPool = new TaskProcessorPool(threads, queue, policy, Utils.HYBRID);
		this.executor = Executors.newCachedThreadPool();
		this.queue = workerPool.start(executor);

		for (SubQuery q : this.subQueries) {
			q.setParent(this);
			q.setup();
			if (q.isMostUpstream())
				this.dispatcher = q.getTaskDispatcher();
		}

		Thread monitor = new Thread(new PerformanceMonitor(this));
		monitor.setName("Monitor");
		monitor.start();
	}

	public int getId() {
		return id;
	}
	
	/*
	public ConcurrentLinkedQueue<ITask> getExecutorQueue() {
		return this.queue;
	}
	*/
	
	public TaskQueue getExecutorQueue() {
		return this.queue;
	}
	
	/*
	public ConcurrentLinkedQueue<ITask> getGPUExecutorQueue() {
		return _queue;
	}
	*/

	public int getExecutorQueueSize() {
		return this.queue.size();
	}

	public Set<SubQuery> getSubQueries() {
		return this.subQueries;
	}
	/*
	public int getSecondExecutorQueueSize() {
		return this._queue.size();
	}
	*/
}
