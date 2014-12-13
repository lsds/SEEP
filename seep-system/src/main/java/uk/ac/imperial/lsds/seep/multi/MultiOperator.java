package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class MultiOperator {
	
	private static int threads = Utils.THREADS;
	
	private Set<SubQuery> subQueries;
	private int id;
	
	private TaskDispatcher  dispatcher;
	private ConcurrentLinkedQueue<Task> queue, _queue;
	private TaskProcessorPool workerPool, _workerPool;
	private Executor executor, _executor;
	
	public MultiOperator (Set<SubQuery> subQueries, int id) {
		this.subQueries = subQueries;
		this.id = id;
	}
	
	public void processData (byte [] values) {
		
		this.dispatcher.dispatch(values);
	}
	
	public void setup () {
		
		if (Utils.HYBRID) {
			/*
			 * Allocate one thread for the GPU executor service, and 
			 * the remaining threads to the CPU executor service.
			 */
			if (threads < 2)
			{
				throw new IllegalArgumentException("error: insufficient number of threads for hybrid execution mode");
			}
			/*
			 * Allocate one out of `threads` workers to the GPU.
			 */
			this._queue = new ConcurrentLinkedQueue<Task> ();
			this._workerPool = new TaskProcessorPool (1, _queue);
			this._executor = Executors.newCachedThreadPool();
			this._queue = _workerPool.start(_executor);
			
			threads --;
		}
		
		this.queue = new ConcurrentLinkedQueue<Task> ();
		this.workerPool = new TaskProcessorPool (threads, queue);
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
	
	public int getId () {
		return id;
	}
	
	public ConcurrentLinkedQueue<Task> getExecutorQueue () {
		return this.queue;
	}
	
	public ConcurrentLinkedQueue<Task> getGPUExecutorQueue () {
		return _queue;
	}
	
	public int getExecutorQueueSize () {
		return this.queue.size();
	}
	
	public Set<SubQuery> getSubQueries () {
		return this.subQueries;
	}

	public int getSecondExecutorQueueSize() {
		return this._queue.size();
	}
}
