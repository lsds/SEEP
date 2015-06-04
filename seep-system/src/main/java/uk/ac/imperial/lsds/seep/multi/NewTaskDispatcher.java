package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayList;

public class NewTaskDispatcher implements ITaskDispatcher {
	
	private static final int _undefined = -1;
	
	/* private ConcurrentLinkedQueue<ITask> workerQueue, _workerQueue; */
	private TaskQueue workerQueue;
	private IQueryBuffer buffer;
	private WindowDefinition window;
	private ITupleSchema schema;
	private ResultHandler handler;
	private SubQuery parent;
	
	private int batchBytes;
	private int batchRecords;
	
	private int batch;
	
	private int nextTask = 1;
	
	/* Some constants for calculating window batches */
	
	long ppb = 0L; /* Panes/batch */
	long tpb = 0L; /* Tuples/batch or time units/batch */
	
	/* Total number of tuples (rows) processed 
	 * (currently, monotonically increasing) */
	long rowCount = 0L;
	/* Temporary pointers */
	long p, q, f;
	long next_, _next; /* Next batch start and end pointers */
	long offset;
	long mask;
	
	boolean first = true;
	
	/* First and last timestamp of a bulk insert */
	long start, end;
	
	int tupleSize;
	
	int [][] batches;
	static final int _START = 0;
	static final int   _END = 1;
	static final int  _FREE = 2;
	int b; /* Next batch to  open */
	int d; /* Next batch to close */
	
	int previous; /* The previous batch opened */
	
	/* Temporary values */
	long tmp;
	int position;
	
	/* The last tuple that we have found in search of opening a window batch */
	int current;
	
	int remainder = 0;
	
	int accumulated = 0;
	
	private ArrayList<Integer> marks;
	
	public NewTaskDispatcher (SubQuery parent) {
		
		this.parent = parent;
		this.buffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
		this.window = this.parent.getWindowDefinition();
		this.schema = this.parent.getSchema();
		
		this.handler = new ResultHandler (this.buffer, parent);
		
		this.batchBytes   = this.parent.getQueryConf().BATCH;
		this.batchRecords = this.parent.getQueryConf()._BATCH_RECORDS;
		
		this.tupleSize = schema.getByteSizeOfTuple();
		
		int tuplesPerBatch = this.batchBytes / this.tupleSize;
		int panesPerBatch = (int) (tuplesPerBatch / window.getPaneSize());
		
		this.batch = ((int) (panesPerBatch - window.numberOfPanes()) / (int) window.panesPerSlide()) + 1;
		
		/* Initialize constants */
		System.out.println(String.format("[DBG] %d windows/batch %d panes/slide %d panes/window", 
			this.batch, window.panesPerSlide(), window.numberOfPanes()));
		
		ppb = window.panesPerSlide() * (this.batch - 1) + 
				window.numberOfPanes();
		
		tpb = ppb * window.getPaneSize();
		
		if (window.isTumbling())
			offset = tpb;
		else
			offset = (this.batch) * window.getSlide(); // (this.batch - 1) * window.getSlide();
		
		p = q = 0L;
		next_ = 0L;
		_next = tpb - 1;
		
		mask = buffer.capacity() - 1;
		
		batches = new int [this.batchRecords][3];
		/* Initialize state */
		for (int i = 0; i < this.batchRecords; i++)
			batches[i][_START] = _undefined;
		b = d = 0;
		
		previous = -1;
		
		marks = new ArrayList<Integer>();
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
		 * long size = (q <= p) ? (q + buffer.capacity()) - p : q - p;
		 * 
		 * System.out.println(
			String.format("[DBG] Query %d Task %6d [%10d, %10d), free %10d, [%6d, %6d] size %10d", 
					parent.getId(), taskid, p, q, free, t_, _t, size)); */
		 
		if (q <= p)
			q += buffer.capacity();
		
		/* Find latency mark */
		int mark = -1;
		for (int i = 0; i < marks.size(); i++) {
			if (marks.get(i) >= ((int) p) && marks.get(i) < ((int) free)) {
				mark = marks.remove(i);
				break;
			}
		}
		
		batch = WindowBatchFactory.newInstance(this.batch, taskid, (int) free, buffer, window, schema, mark);
		if (window.isRangeBased()) {
			if (getTimestamp(buffer, (int) p) > _t)
				batch.cancel();
			else
				batch.setBatchPointers((int) p, (int) q);
			batch.setRange(t_, _t);
		} else
			batch.setBatchPointers((int) p, (int) q);
		
		/*
		batch.initWindowPointers();
		batch.debug();
		*/
		task = TaskFactory.newInstance(parent, batch, handler, taskid, (int) free);
		workerQueue.add(task);
	}
	
	private void assemble (int index, int length) {
		
		if (Utils.LATENCY_ON)
			marks.add(index);
		
		/* Number of rows added */
		int rows = length / tupleSize;
		
		int index_ = index - remainder;
		
		/* Consider length from index_, +length bytes */
		int _length = (int) Math.floor((double) (length + remainder) / (double) tupleSize) * tupleSize;
		remainder = (length + remainder) - _length;
		
		/* Index of the last tuple inserted in the circular buffer */
		int _index = (int) ((index_ + _length - tupleSize) & mask);
		
		if (window.isRowBased()) {
			while ((rowCount + rows) >= _next + 1) {
				/* Set start and end pointers for batch */
				p = ((next_) * tupleSize) & mask;
				q = ((_next + 1) * tupleSize) & mask;
				q = (q == 0) ? buffer.capacity() : q;
				/* Set free pointer */
				f = (p + (offset * tupleSize)) & mask;
				f = (f == 0) ? buffer.capacity() : f;
				f--;
				/* Dispatch task */
				this.newTaskFor (p, q, f, _undefined, _undefined);
				if (window.isTumbling()) {
					next_ += offset;
					_next += tpb;
				}
				else {
					next_ += offset;
					_next += ((this.batch) * window.getSlide()); // ((this.batch - 1) * window.getSlide());
				}
			}
		} else
		if (window.isRangeBased()) {
			
			accumulated += _length;
			if (accumulated < batchBytes)
				return;
			/* We have accumulated enough data for a batch.
			 * 
			 * `next_` points to the beginning of this batch.
			 * 
			 * `_index` points to the last tuple inserted.
			 * 
			 * We can not include the last tuple inserted into 
			 * the current batch because we cannot know if the 
			 * window it belongs to is closed.
			 * 
			 * So, the last time stamp of the current batch is
			 * at best `end - 1`.
			 */
			start = getTimestamp(buffer, (int) next_);
			end   = getTimestamp(buffer, _index);
			/*
			 * We can find out how many windows are in this batch.
			 */
			int panesPerBatch = (int) (end - 1 - start) / (int) window.getPaneSize();
			this.batch = ((int) (panesPerBatch - window.numberOfPanes()) / (int) window.panesPerSlide()) + 1;
			
			/* Calculate again the end pointer */
			ppb = window.panesPerSlide() * (this.batch - 1) + 
					window.numberOfPanes();
			
			tpb = ppb * window.getPaneSize();
			
			end = tpb;
			/*
			 * We need to find the end pointer of this batch,
			 * which is the first occurrence of timestamp `end`.
			 */
			tmp = next_;
			position = firstOccurenceOf (end, (int) next_, _index, (int) next_);
			/* Position is the free pointer */
			
			/* I guess we have to scan linearly to find the */
			
		} else
		{
			throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
		}
		rowCount += rows;
	}
	
	private int incrementAndGet (int x, boolean check) {
		int value = ++x & (this.batchRecords - 1);
		/* We treat `batches` as an unsafe circular buffer. But, without 
		 * sufficient capacity, we may ovewrite the pointers of a window 
		 * batch that is currently open.
		 */
		if (check)
			if (batches[value][_START] >= 0)
				throw new IllegalStateException(String.format("error: batch %d is currently open", value));
		return value;
	}
	
	
	private int firstOccurenceOf (long t, int start, int end, int offset) {
		int position;
		if ((position = binarySearch(t, start, end, offset)) < 0)
			return -1;
		return scanLeft (t, position, offset);
	}
	
	private int scanLeft (long t, int index, int offset) {
		/*
		 * The `index` points to the location in the byte buffer of a tuple 
		 * whose timestamp is `t`. But, is this tuple the first one?
		 */
		
		while (index >= offset && getTimestamp (buffer, index) == t) {
			index -= tupleSize;
		}
		index += tupleSize;
		return index;
	}

	private int binarySearch (long t, int start, int end, int offset) {
		/*
		 * The `start` and `end` pointers represent tuple and not byte indices
		 */
		while (start <= end) {
			int m = start + (end - start) / 2;
			/* Normalize tuple offset in byte buffer */
			int y = (int) ((offset + m * tupleSize) & mask);
			if (t < getTimestamp (buffer, y)) 
				end = m - 1;
			else 
			if (t > getTimestamp (buffer, y))
				start = m + 1;
			else {
				this.current = m;
				return y;
			}
		}
		return -1;
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
	public IQueryBuffer getSecondBuffer() {
		/* throw new UnsupportedOperationException("Cannot get second buffer since this is a single-input dispatcher"); */
		return null;
	}

	@Override
	public boolean tryDispatchFirst(byte[] data, int length) {
		
		return tryDispatch (data, length);
	}

	@Override
	public boolean tryDispatchSecond(byte[] data, int length) {
		
		return tryDispatch (data, length);
	}
	
	private long getTimestamp (IQueryBuffer buffer, int index) {
		long value = buffer.getLong(0);
		if (Utils.LATENCY_ON)
			return (long) Utils.unpack(0, value);
		else 
			return value;
	}

	@Override
	public long getBytesGenerated() {
		
		return this.handler.getTotalOutputBytes();
	}

	@Override
	public int getWindowStateSize() {
		// TODO Auto-generated method stub
		return 0;
	}
}

