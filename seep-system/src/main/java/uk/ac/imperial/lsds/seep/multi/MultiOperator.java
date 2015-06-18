package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MultiOperator {

	private static int threads = Utils.THREADS;
	
	private Set<SubQuery> subQueries;
	private int id;
	
	/*
	 * At the top level, the input stream will be stream
	 * will be dispatched to N subqueries, where:
	 * 
	 * N < `_max_upstream_subqueries`
	 */
	private static final int _max_upstream_subqueries = 2;
	
	private ITaskDispatcher	[] dispatcher;
	private int freeIndex = 0;
	
	private TaskQueue queue;
	private TaskProcessorPool workerPool;
	private Executor executor;
	
	int nqueries = 1;
	int nclasses = 2; /* CPU and GPU */
	
	int [][] policy;

	public MultiOperator (Set<SubQuery> subQueries, int id) {
		
		this.subQueries = subQueries;
		this.id = id;
		
		this.dispatcher = new ITaskDispatcher [_max_upstream_subqueries];
		this.freeIndex = 0;
		
		this.nqueries = this.subQueries.size();
	}
	
	public void processData (byte [] values) {
		
		processData (values, values.length);
	}
	
	public void processData (byte [] values, int length) {
		
		for (int i = 0; i < freeIndex; i++)
			this.dispatcher[i].dispatch (values, length);
	}

	public void processDataSecond (byte [] values) {
		
		processDataSecond (values, values.length);
	}
	
	public void processDataSecond (byte [] values, int length) {
		
		for (int i = 0; i < freeIndex; i++)
			this.dispatcher[i].dispatchSecond(values, length);
	}
	
	public void setup() {
		
		this.policy = new int [nclasses][nqueries];
		for (int i = 0; i < nclasses; i++) {
			for (int j = 0; j < nqueries; j++) {
				policy [i][j] = 1;
			}
		}
		
		this.queue = new TaskQueue(threads, nqueries);
		
		this.workerPool = new TaskProcessorPool(threads, queue, policy, Utils.HYBRID);
		this.executor = Executors.newCachedThreadPool();
		this.queue = workerPool.start(executor);

		for (SubQuery q : this.subQueries) {
			if (freeIndex >= _max_upstream_subqueries)
				throw new ArrayIndexOutOfBoundsException("error: invalid number of queries in multi-operator");
			q.setParent(this);
			q.setup();
			if (q.isMostUpstream())
				this.dispatcher[freeIndex++] = q.getTaskDispatcher();
		}
		
		Thread monitor = new Thread(new PerformanceMonitor(this));
		monitor.setName("Monitor");
		monitor.start();
	}
	
	public int getId () {
		
		return id;
	}
	
	public TaskQueue getExecutorQueue() {
		
		return this.queue;
	}
	
	public int getExecutorQueueSize() {
		
		return this.queue.size();
	}

	public Set<SubQuery> getSubQueries() {
		
		return this.subQueries;
	}
	
	public TaskProcessorPool getTaskProcessorPool () {
		
		return workerPool; 
	}

	public void updatePolicy (int [][] policy_) {
		
		for (int i = 0; i < nclasses; i++)
			for (int j = 0; j < nqueries; j++)
				policy[i][j] = policy_[i][j];
	}

	public String policyToString () {
		StringBuilder b = new StringBuilder("[");
		for (int i = 0; i < nclasses; i++) {
			for (int j = 0; j < nqueries; j++) {
				if (i == nclasses && j == nqueries)
					b.append(String.format("[%d][%d]=%5d",  i, j, policy[i][j]));
				else
					b.append(String.format("[%d][%d]=%5d ", i, j, policy[i][j]));
			}
		}
		b.append("]");
		return b.toString();
	}
}
