package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class Task implements ITask {

	private WindowBatch		batch;
	private ResultHandler	handler;
	private int				freeUpTo;
	private SubQuery		query;
	
	public int taskid;
	public int queryid;
	public AtomicMarkableReference<Task> next;
	
	private boolean	GPU	= false;

	@Override
	public void setGPU(boolean GPU) {
		this.GPU = GPU;
	}

	public Task() {
		this(null, null, null, 0, 0, 0);
	}

	public Task(SubQuery query, WindowBatch batch, ResultHandler handler,
			int taskid, int freeUpTo, int queryid) {
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.taskid = taskid;
		this.freeUpTo = freeUpTo;
		this.queryid = queryid;
		this.next = new AtomicMarkableReference<Task>(null, false);
	}

	public void set(SubQuery query, WindowBatch batch, ResultHandler handler,
			int taskid, int freeUpTo) {
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.taskid = taskid;
		this.freeUpTo = freeUpTo;
		this.queryid = queryid;
		this.next.set(null, false);
	}

	@Override
	public int run() {
		
		MicroOperator next = query.getMostUpstreamMicroOperator();

		while (next != null) {
			next.process(this.batch, this, GPU);
			next = next.getLocalDownstream();
		}
		
		// System.out.println (String.format("[DBG] free task %4d", this.batch.getTaskId()));
		
		// this.batch.getBuffer().release();
		
		ResultCollector.forwardAndFree(handler, query, this.batch.getBuffer(),
				this.batch.getTaskId(), this.batch.getFreeOffset(), GPU);
		
		// this.batch.getBuffer().release();
		
		WindowBatchFactory.free(this.batch);

		return 0;
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.batch = windowBatchResult;
		/* Control returns to run() method */
	}
	
	@Override
	public void free() {
		TaskFactory.free(this);
	}
}
