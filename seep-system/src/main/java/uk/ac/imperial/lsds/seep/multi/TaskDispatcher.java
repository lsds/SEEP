package uk.ac.imperial.lsds.seep.multi;

public class TaskDispatcher implements ITaskDispatcher {
	
	private static final int _undefined = -1;
	
	/* private ConcurrentLinkedQueue<ITask> workerQueue, _workerQueue; */
	private TaskQueue workerQueue;
	private IQueryBuffer buffer;
	private WindowDefinition window;
	private ITupleSchema schema;
	private ResultHandler handler;
	private SubQuery parent;
	
	private int batch;
	private int batchRecords;
	
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
	
	
	public TaskDispatcher (SubQuery parent) {
		
		this.parent = parent;
		this.buffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
		this.window = this.parent.getWindowDefinition();
		this.schema = this.parent.getSchema();
		
		this.handler = new ResultHandler (this.buffer, parent);
		
		this.batch        = this.parent.getQueryConf().BATCH;
		this.batchRecords = this.parent.getQueryConf()._BATCH_RECORDS;
		
		/* Initialise constants */
		System.out.println(String.format("[DBG] %d panes/slide %d panes/window", window.panesPerSlide(), window.numberOfPanes()));
		ppb = window.panesPerSlide() * (this.batch - 1) + 
				window.numberOfPanes();
		
		tpb = ppb * window.getPaneSize();
		
		if (window.isTumbling())
			offset = tpb;
		else
			offset = (this.batch) * window.getSlide(); // (this.batch - 1) * window.getSlide();
		
		// System.out.println("[DBG] offset is " + offset);
		
		p = q = 0L;
		next_ = 0L;
		_next = tpb - 1;
		
		mask = buffer.capacity() - 1;
		
		tupleSize = schema.getByteSizeOfTuple();
		
		batches = new int [this.batchRecords][3];
		/* Initialise state */
		for (int i = 0; i < this.batchRecords; i++)
			batches[i][_START] = _undefined;
		b = d = 0;
		
		previous = -1;
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
		
		long size = (q <= p) ? (q + buffer.capacity()) - p : q - p;
		
		/* System.out.println(
			String.format("[DBG] Query %d Task %6d [%10d, %10d), free %10d, [%6d, %6d] size %10d", 
					parent.getId(), taskid, p, q, free, t_, _t, size)); */
		 
		if (q <= p)
			q += buffer.capacity();
		
		batch = WindowBatchFactory.newInstance(this.batch, taskid, (int) free, buffer, window, schema);
		if (window.isRangeBased()) {
			if (buffer.getLong((int) p) > _t)
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
			/* Get the timestamp of the first and last tuple inserted */
			start = buffer.getLong(index_);
			end   = buffer.getLong(_index);
			
			if (first) {
				next_  = start;
				_next += next_;
				first = false;
			}
			
			/* System.out.println(String.format("[DBG] range-based window: start %16d end %16d index %10d _index %10d length %10d next_ %16d", 
					start, end, index_, _index, length, next_)); */
			
			/* Open one or more window batches */
			current = 0;
			while (end >= next_) {
				/* Let's assume that we insert a sequence a tuples, marked 'x'
				 * whose timestamps are 1, 2, 2, and 8.
				 * 
				 * The slide of the window is 1, so we wish to open windows at
				 * times 1, 2, 3, and so on.
				 * 
				 * For t = 1 and t = 2, everything is fine. For t = 3, though,
				 * there is no tuple with such timestamp.
				 * 
				 * When does the batch open? It may open at time 4, 5, 6...So,
				 * one way is to search for t + 1, t + 2, and so on, until we
				 * find the next tuple with a timestamp greater than t = 3.
				 * 
				 * x---xx-----------------x---------------------> t
				 * 
				 * |--|--|--|--|--|--|
				 * 1                 7
				 *    |--|--|--|--|--|--|
				 *    2                 8
				 *       |--|--|--|--|--|--|
				 *       3                 9
				 */
				tmp = next_;
				while ((position = firstOccurenceOf (tmp, current, rows, index_)) < 0)
					tmp ++;
				
				/* Set the start pointer for this window batch */
				batches[b][_START] = position;
				
				/* 
				 * Set the free pointer for the previous window batch, if any. 
				 * If position is 0, then the free pointer should point at the 
				 * end of the buffer, otherwise `position--` will be negative.
				 */
				position = (position == 0) ? buffer.capacity() : position;
				position --;
				if (previous >= 0)
					batches[previous][_FREE] = position;
				/* Set counters for the next batch to open */
				previous = b;
				b = incrementAndGet(b, true);
				next_ += offset;
			}
			/* Should we close old batches? 
			 * 
			 * If a window batch close at time `t`, we are looking for the
			 * first element whose timestamp is greater than `t`.
			 *  
			 */
			current = 0;
			while (end >= (_next + 1)) {
				
				tmp = _next + 1;
				while ((position = firstOccurenceOf (tmp, current, rows, index_)) < 0)
					tmp ++;
				
				/* Set the end pointer for this window batch */
				batches[d][_END] = position;
				
				/* Dispatch a task */
				this.newTaskFor (
						batches[d][_START], 
						batches[d][  _END], 
						batches[d][ _FREE], 
						_next - tpb + 1, _next
						);
				batches[d][_START] = _undefined;
				/* Set counters for the next batch to close */
				d = incrementAndGet(d, false);
				_next += offset;
			}
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
		while (index >= offset && buffer.getLong(index) == t) {
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
			if (t < buffer.getLong(y)) 
				end = m - 1;
			else 
			if (t > buffer.getLong(y))
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
}

