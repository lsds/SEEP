package uk.ac.imperial.lsds.seep.multi;

public class Task implements IWindowAPI {

	private WindowBatch batch;
	private ResultHandler handler;
	private int taskid;
	private int freeUpTo;
	private SubQuery query;
	
	private boolean GPU = false;
	
	public void setGPU(boolean GPU) {
		this.GPU = GPU;
	}
	
	public Task() {
		this(null, null, null, 0, 0);
	}

	public Task(SubQuery query, WindowBatch batch, ResultHandler handler,
			int taskid, int freeUpTo) {
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.taskid = taskid;
		this.freeUpTo = freeUpTo;
	}

	public void set(SubQuery query, WindowBatch batch, ResultHandler handler,
			int taskid, int freeUpTo) {
		this.query = query;
		this.batch = batch;
		this.handler = handler;
		this.taskid = taskid;
		this.freeUpTo = freeUpTo;
	}

	public int run() {
		IQueryBuffer buffer = batch.getBuffer();
		
		query.getMostUpstreamMicroOperator().process(batch, this, GPU);
		
		ResultCollector
				.forwardAndFree(handler, query, buffer, taskid, freeUpTo);

		this.batch.getBuffer().release();
		WindowBatchFactory.free(this.batch);

		return 0;
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.batch = windowBatchResult;
		/* Control returns to run() method */
	}
}
