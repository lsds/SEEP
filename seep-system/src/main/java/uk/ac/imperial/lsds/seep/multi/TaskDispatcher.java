package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TaskDispatcher implements ITaskDispatcher {
	
	private TaskQueue workerQueue;
	private IQueryBuffer buffer;
	private WindowDefinition window;
	private ITupleSchema schema;
	private ResultHandler handler;
	private SubQuery parent;
	
	private int batchBytes;
	
	private int nextTask = 1;
	
	/* Total number of tuples (rows) processed 
	 * (currently, monotonically increasing) */
	long rowCount = 0L;
	
	/* Temporary pointers */
	long f;
	long mask;
	
	boolean first = true;
	
	int tupleSize;
	
	int remainder = 0;
	
	private int [] marks;
	private int setMark = 0;
	private int getMark = 0;
	
	long accumulated = 0;
	
	long nextBatchEndPointer = 0;
	
	long thisBatchStartPointer = 0;
	
	public TaskDispatcher (SubQuery parent) {
		
		this.parent = parent;
		this.buffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
		this.window = this.parent.getWindowDefinition();
		this.schema = this.parent.getSchema();
		
		this.handler = new ResultHandler (this.buffer, parent);
		
		this.batchBytes = this.parent.getQueryConf().BATCH;
		
		this.tupleSize = schema.getByteSizeOfTuple();
		
		/* Initialize constants */
		System.out.println(String.format("[DBG] %d bytes/batch %d panes/slide %d panes/window", 
				batchBytes, window.panesPerSlide(), window.numberOfPanes()));
		
		mask = buffer.capacity() - 1;
		
		tupleSize = schema.getByteSizeOfTuple();
		
		marks = new int [1024];
		Arrays.fill(marks, -1);
		setMark = getMark = 0;
		
		nextBatchEndPointer = batchBytes;
	}
	
	private void incSetMark () {
		setMark ++;
		if (setMark >= marks.length)
			setMark = 0;
	}
	
	private void incGetMark () {
		getMark ++;
		if (getMark >= marks.length)
			getMark = 0;
	}
	
	@Override
	public void setup () {
		/* The default task queue for either CPU or GPU executor */
		this.workerQueue = this.parent.getExecutorQueue();
	}
	
	@Override
	public void dispatch (byte [] data, int length) {
		int idx;
		while ((idx = buffer.put(data, length)) < 0) {
			Thread.yield();
		}
		assemble (idx, length);
	}
	
	@Override
	public boolean tryDispatch(byte [] data, int length) {
		int idx;
		if ((idx = buffer.put(data, length)) < 0) {
			return false;
		}
		assemble (idx, length);
		return true;
	}
	
	private void newTaskFor (long p, long q, long free, long t_, long _t) {
		Task task;
		WindowBatch batch;
		int taskid;
		
		taskid = this.getTaskNumber();
		
		/*
		long size = (q <= p) ? (q + buffer.capacity()) - p : q - p;
		System.out.println(
			String.format("[DBG] Query %d Task %6d [%10d, %10d), free %10d, [%6d, %6d] size %10d", 
					parent.getId(), taskid, p, q, free, t_, _t, size));
		*/
		
		if (q <= p) {
			q += buffer.capacity();
		}
		
		/* Find latency mark */
		int mark = -1;
		if (Utils.LATENCY_ON) {
			mark = marks[getMark];
			marks[getMark] = -1;
			incGetMark();
			while (marks[getMark] >= ((int) p) && marks[getMark] < ((int) free)) {
				marks[getMark] = -1;
				incGetMark();
			}
			if (! (mark >= ((int) p) && mark < ((int) free))) {
				System.err.println("fatal error: invalid latency mark");
				System.exit(1);
			}
			/* System.out.println(String.format("[DBG] mark %d next index is %d", 
			 * mark, getMark));
			 */
		}
		
		/* Update free pointer */
		free -= schema.getByteSizeOfTuple();
		if (free < 0) {
			System.err.println(String.format("error: negative free pointer (%d) for query %d", free, parent.getId()));
			long size = (q <= p) ? (q + buffer.capacity()) - p : q - p;
			System.out.println(
					String.format("[DBG] Query %d Task %6d [%10d, %10d), free %10d, [%6d, %6d] size %10d", 
							parent.getId(), taskid, p, q, free, t_, _t, size));
			System.exit(1);
		}
		
		batch = WindowBatchFactory.newInstance(this.batchBytes, taskid, (int) (free), buffer, window, schema, mark);
		
		if (window.isRangeBased()) {
			long startTime = getTimestamp(buffer, (int) p);
			long endTime   = getTimestamp(buffer, (int) q - tupleSize);
			batch.setBatchTime(startTime, endTime);
		} else {
			batch.setBatchTime(-1, -1);
		}
		batch.setBufferPointers((int) p, (int) q);
		batch.setBatchPointers (t_, _t);

		task = TaskFactory.newInstance(parent, batch, handler, taskid, (int) free);
		
		workerQueue.add(task);
	}
	
	private void assemble (int index, int length) {
		
		if (Utils.LATENCY_ON) {
			/* System.out.println(String.format("[BG] new mark at %d", index)); */
			marks[setMark] = index;
			incSetMark();
		}
		
		/* Number of rows added */
		int rows = length / tupleSize;
		
		/* int index_ = index - remainder; */
		
		/* Consider length from index_, +length bytes */
		int _length = (int) Math.floor((double) (length + remainder) / (double) tupleSize) * tupleSize;
		remainder = (length + remainder) - _length;
		
		/* Index of the last tuple inserted in the circular buffer:
		 * 
		 * int _index = (int) ((index_ + _length - tupleSize) & mask); */
		
		if (window.isRangeBased() || window.isRowBased()) {
			
			/*
			 * Inserted `length` bytes, from `index_` to `_index`.
			 */
			
			if (first) {
				thisBatchStartPointer = 0;
				nextBatchEndPointer = batchBytes;
				first = false;
			}
			
			accumulated += length;
			
			while (accumulated >= nextBatchEndPointer) {
				
				f = nextBatchEndPointer & mask;
				f = (f == 0) ? buffer.capacity() : f;
				f--;
				/* Launch task */
				this.newTaskFor (
					thisBatchStartPointer & mask, 
					nextBatchEndPointer & mask, 
					f, 
					thisBatchStartPointer, nextBatchEndPointer
					);
				
				thisBatchStartPointer = thisBatchStartPointer + batchBytes;
				nextBatchEndPointer = nextBatchEndPointer + batchBytes;
			}
		} else
		{
			throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
		}
		rowCount += rows;
	}
	
	private int getTaskNumber () {
		int id = nextTask ++;
		if (nextTask == Integer.MAX_VALUE)
			nextTask = 1;
		return id;
	}
	
	@Override
	public IQueryBuffer getBuffer () {
		return this.buffer;
	}

	@Override
	public void dispatchSecond (byte [] data, int length) {
		throw new UnsupportedOperationException("Cannot dispatch to second buffer since this is a single-input dispatcher");
	}

	@Override
	public IQueryBuffer getSecondBuffer () {
		/* throw new UnsupportedOperationException("Cannot get second buffer since this is a single-input dispatcher"); */
		return null;
	}

	@Override
	public boolean tryDispatchFirst(byte[] data, int length) {
		
		return tryDispatch (data, length);
	}

	@Override
	public boolean tryDispatchSecond(byte[] data, int length) {
		
		return tryDispatchSecond (data, length);
	}
	
	private long getTimestamp (IQueryBuffer buffer, int index) {
		long value = buffer.getLong(index);
		if (Utils.LATENCY_ON)
			return (long) Utils.unpack(1, value);
		else 
			return value;
	}

	@Override
	public long getBytesGenerated() {
		
		return handler.getTotalOutputBytes();
	}
	
	public ResultHandler getHandler () {
		
		return handler;
	}

	@Override
	public void setAggregateOperator(IAggregateOperator operator) {
		
		this.handler.setAggregateOperator (operator);
	}
}
