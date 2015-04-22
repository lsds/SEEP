package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class Task extends ITask {
	
	private SubQuery query;
	private WindowBatch batch;
	private ResultHandler handler;
	private int freeIndex;
	
	public Task() {
		
		this(null, null, null, 0, 0, 0);
	}
	
	public Task (SubQuery query, WindowBatch batch, ResultHandler handler, int taskid, int freeIndex, int queryid) {
		
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.setFreeIndex(freeIndex);
		
		this.taskid = taskid;
		this.queryid = queryid;
		this.next = new AtomicMarkableReference<ITask>(null, false);
	}

	public void set (SubQuery query, WindowBatch batch, ResultHandler handler, int taskid, int freeIndex) {
		
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.setFreeIndex(freeIndex);
		
		this.taskid = taskid;
		this.queryid = query.getId();
		this.next.set(null, false);
	}
	
	@Override
	public int run() {
		
		MicroOperator next = query.getMostUpstreamMicroOperator();

		while (next != null) {
			next.process(this.batch, this, GPU);
			next = next.getLocalDownstream();
		}
		
		if (GPU) {
			ResultCollector.forwardAndFree (handler, _query, this.batch.getBuffer(),
					this.batch.getTaskId(), this.batch.getFreeOffset(), this.batch.getLatencyMark(), GPU);
		} else {
			ResultCollector.forwardAndFree (handler,  query, this.batch.getBuffer(),
					this.batch.getTaskId(), this.batch.getFreeOffset(), this.batch.getLatencyMark(), GPU);
		}
		
		WindowBatchFactory.free(this.batch);
		return 0;
	}
	
	@Override
	public void outputWindowBatchResult(int streamID, WindowBatch windowBatchResult) {
		
		this.batch = windowBatchResult;
		/* Control returns to run() method */
	}
	
	@Override
	public void free() {
		TaskFactory.free(this);
	}

	public int getFreeIndex() {
		return freeIndex;
	}

	public void setFreeIndex(int freeIndex) {
		this.freeIndex = freeIndex;
	}

	@Override
	public SubQuery getQuery() {
		return query;
	}
}
