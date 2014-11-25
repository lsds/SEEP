package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.Callable;


public class Task implements Callable<Integer>, IWindowAPI {
	
	private WindowBatch batch;
	private ResultHandler handler;
	private int taskid;
	private int freeUpTo;
	private SubQuery query;
	
	public Task () {
		this(null, null, null, 0, 0);
	}
	
	public Task (SubQuery query, WindowBatch batch, ResultHandler handler, int taskid, int freeUpTo) {
		this.query    =    query;
		this.batch    =    batch;
		this.handler  =  handler;
		this.taskid   =   taskid;
		this.freeUpTo = freeUpTo;
	}
	
	public void set (SubQuery query, WindowBatch batch, ResultHandler handler, int taskid, int freeUpTo) {
		this.query    =    query;
		this.batch    =    batch;
		this.handler  =  handler;
		this.taskid   =   taskid;
		this.freeUpTo = freeUpTo;
	}
	
	@Override
	public Integer call () throws Exception {
		IQueryBuffer buffer = batch.getBuffer();
		int start = batch.getBatchStartPointer();
		int end = batch.getBatchEndPointer();
		int tupleSize = batch.getSchema().getByteSizeOfTuple();
		int result = 0;
		for (int index = start; index < end; index += tupleSize) {
			result += buffer.getLong(index);
		}
		
		query.getMostUpstreamMicroOperator().process(batch, this);
		
		
		/* System.out.println(String.format("[DBG] [Task %7d] %d", taskid, result)); */
		ResultCollector.free(handler, buffer, taskid, freeUpTo);
		return result;
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.batch = windowBatchResult;
	}
}
