package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskDispatcher {
	
	private ConcurrentLinkedQueue<Task> workerQueue;
	private IQueryBuffer buffer;
	private WindowDefinition window;
	private ITupleSchema schema;
	private ResultHandler handler;
	private SubQuery parent;
	
	private int nextTask = 0;
	
	/* Some constants for calculating window batches */
	
	long ppb = 0L; /* Panes/batch  */
	long tpb = 0L; /* Tuples/batch */
	
	/* Total number of tuples (rows) processed (currently, monotonically increasing) */
	long rowCount = 0L;
	/* Temporary pointers */
	long p, q;
	long next_, _next; /* Next batch start and end pointers */
	
	int tupleSize;
	
	public TaskDispatcher (SubQuery parent) {
		
		this.parent = parent;
		this.buffer = new CircularQueryBuffer(Utils._CIRCULAR_BUFFER_);
		this.window = this.parent.getWindowDefinition();
		this.schema = this.parent.getSchema();
		this.handler = new ResultHandler ();
		
		/* Initialise constants */
		ppb = window.panesPerSlide() * (Utils.BATCH - 1) + window.numberOfPanes();
		if (window.isRowBased()) 
		{
			tpb = ppb * window.getPaneSize();
		}
		p = q =  0L;
		next_ =  0L;
		_next = tpb;
		
		tupleSize = schema.getByteSizeOfTuple();
	}
	
	public void setup () {
		this.workerQueue = this.parent.getExecutorQueue();
	}
	
	public void dispatch (byte [] data) {
		int idx;
		while ((idx = buffer.put(data)) < 0) {
			Thread.yield();
			/* LockSupport.parkNanos(10L); */
		}
		assemble (idx, data.length);
	}
	
	private void newTaskFor (long p, long q, long free) {
		Task task;
		WindowBatch batch;
		/* System.out.println(String.format("[%10d, %10d)", p, q)); */
		batch = WindowBatchFactory.newInstance(Utils.BATCH, buffer, window, schema, (int) p, (int) q);
		task = TaskFactory.newInstance(parent, batch, handler, this.getTaskNumber(), (int) free);
		workerQueue.add(task);
	}
	
	private void assemble (int index, int length) {
		
		
		if (window.isRowBased()) {
			/* Number of rows added */
			int rows = length / tupleSize;
			if (window.isTumbling()) {
				while ((rowCount + rows) >= _next) {
					p = (next_ * tupleSize) & (buffer.capacity() - 1);
					q = (_next * tupleSize) & (buffer.capacity() - 1);
					q = (q == 0) ? buffer.capacity() : q;
					/* Dispatch task */
					this.newTaskFor (p, q, q - 1);
					next_ += tpb;
					_next += tpb;
				}
			} else {
				throw new UnsupportedOperationException("error: support for row-based sliding windows not yet implemented");
			}
			rowCount += rows;
		} else
		if (window.isRangeBased()) {
			if (window.isTumbling()) {
				
			} else {
				throw new UnsupportedOperationException("error: support for range-based sliding windows not yet implemented");
			}
		} else
		{
			throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
		}
	}
	
	private int getTaskNumber () {
		int id = nextTask ++;
		if (nextTask == Integer.MAX_VALUE)
			nextTask = 0;
		return id;
	}
	
	public IQueryBuffer getBuffer () {
		return this.buffer;
	}
}

