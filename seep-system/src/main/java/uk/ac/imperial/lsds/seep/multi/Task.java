package uk.ac.imperial.lsds.seep.multi;

public class Task implements IWindowAPI {

	private WindowBatch batch;
	private ResultHandler handler;
	private int taskid;
	private int freeUpTo;
	private SubQuery query;

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
		int result = 0;

		int start = batch.getBatchStartPointer();
		int end = batch.getBatchEndPointer();
		int tupleSize = batch.getSchema().getByteSizeOfTuple();

		for (int index = start; index < end; index += tupleSize) {
			result += buffer.getLong(index);
		}
		/*
		 * query.getMostUpstreamMicroOperator().process(batch, this);
		 */

		/*
		 * System.out.println(String.format("[DBG] [Task %7d] %d", taskid,
		 * result));
		 */

		ResultCollector
				.forwardAndFree(handler, query, buffer, taskid, freeUpTo);

		this.batch.getBuffer().release();
		WindowBatchFactory.free(this.batch);

		return result;
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.batch = windowBatchResult;
		this.batch.getBuffer().release();
		WindowBatchFactory.free(this.batch);
	}
}
