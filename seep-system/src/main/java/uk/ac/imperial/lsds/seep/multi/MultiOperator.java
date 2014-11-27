package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class MultiOperator {
	
	private static final int threads = Utils.THREADS;
	private static final int queue_size = Utils.TASKS;
	
	private Set<SubQuery> subQueries;
	private int id;
	
	private TaskDispatcher  dispatcher;
	private ConcurrentLinkedQueue<Task> _queue, queue;
	private TaskProcessorPool workerPool;
	private Executor executor;
	
	public MultiOperator (Set<SubQuery> subQueries, int id) {
		this.subQueries = subQueries;
		this.id = id;
	}
	
	public void processData (byte [] values) {
		
		this.dispatcher.dispatch(values);
	}
	
	public void setup () {
		
		this._queue = new ConcurrentLinkedQueue<Task> ();
		this.workerPool = new TaskProcessorPool (threads, _queue);
		this.executor = Executors.newCachedThreadPool();
		queue = workerPool.start(executor);
		
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
	
	public int getExecutorQueueSize () {
		return this.queue.size();
	}
	
	public Set<SubQuery> getSubQueries () {
		return this.subQueries;
	}
}
