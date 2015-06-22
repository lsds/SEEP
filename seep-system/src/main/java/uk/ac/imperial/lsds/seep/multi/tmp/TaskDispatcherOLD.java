package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.multi.IAggregateOperator;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITaskDispatcher;

public class TaskDispatcherOLD implements ITaskDispatcher {

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispatch(byte[] data, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tryDispatch(byte[] data, int length) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispatchSecond(byte[] data, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IQueryBuffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQueryBuffer getSecondBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean tryDispatchFirst(byte[] data, int length) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tryDispatchSecond(byte[] data, int length) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getBytesGenerated() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAggregateOperator(IAggregateOperator operator) {
		// TODO Auto-generated method stub
		
	}
	
//	private static final int _undefined = -1;
//	
//	/* private ConcurrentLinkedQueue<ITask> workerQueue, _workerQueue; */
//	private TaskQueue workerQueue;
//	private IQueryBuffer buffer;
//	private WindowDefinition window;
//	private ITupleSchema schema;
//	private ResultHandler handler;
//	private SubQuery parent;
//	
//	private int batch;
//	
//	private int batchBytes;
//	private int batchRecords;
//	
//	private int nextTask = 1;
//	
//	/* Some constants for calculating window batches */
//	
//	long ppb = 0L; /* Panes/batch */
//	long tpb = 0L; /* Tuples/batch or time units/batch */
//	
//	/* Total number of tuples (rows) processed 
//	 * (currently, monotonically increasing) */
//	long rowCount = 0L;
//	/* Temporary pointers */
//	long p, q, f;
//	long next_, _next; /* Next batch start and end pointers */
//	long offset;
//	long mask;
//	
//	boolean first = true;
//	
//	/* First and last timestamp of a bulk insert */
//	long start, end;
//	
//	int tupleSize;
//	
//	int [][] batches;
//	static final int _START = 0;
//	static final int   _END = 1;
//	static final int  _FREE = 2;
//	int b; /* Next batch to  open */
//	int d; /* Next batch to close */
//	
//	int previous; /* The previous batch opened */
//	
//	/* Temporary values */
//	long tmp;
//	int position;
//	
//	/* The last tuple that we have found in search of opening a window batch */
//	int current;
//	
//	int remainder = 0;
//	
//	private long count = 0L;
//	private long starttime;
//	private double  dt;
//	double _m, m, _s, s;
//	double avg = 0D, std = 0D;
//	
//	private ArrayList<Integer> marks;
//	
//	long accumulated = 0;
//	
//	long nextBatchStartPointer = 0;
//	long nextBatchEndPointer = 0;
//	
//	long thisBatchStartPointer = 0;
//	long thisBatchEndPointer = 0;
//	long thisBatchFreePointer = 0;
//	
//	long batchStartTimestamp = 0;
//	long batchEndTimestamp = 0;
//	
//	public TaskDispatcherOLD (SubQuery parent) {
//		
//		this.parent = parent;
//		this.buffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
//		this.window = this.parent.getWindowDefinition();
//		this.schema = this.parent.getSchema();
//		
//		this.handler = new ResultHandler (this.buffer, parent);
//		
//		this.batchBytes   = this.parent.getQueryConf().BATCH;
//		this.batchRecords = this.parent.getQueryConf()._BATCH_RECORDS;
//		
//		this.tupleSize = schema.getByteSizeOfTuple();
//		
//		int tuplesPerBatch = this.batchBytes / this.tupleSize;
//		int panesPerBatch = (int) (tuplesPerBatch / window.getPaneSize());
//		
//		this.batch = ((int) (panesPerBatch - window.numberOfPanes()) / (int) window.panesPerSlide()) + 1;
//		// this.batch = this.batchBytes;
//		
//		/* Initialise constants */
//		System.out.println(String.format("[DBG] %d windows %d panes/slide %d panes/window", 
//				batch, window.panesPerSlide(), window.numberOfPanes()));
//		
//		ppb = window.panesPerSlide() * (this.batch - 1) + 
//				window.numberOfPanes();
//		
//		tpb = ppb * window.getPaneSize();
//		
//		if (window.isTumbling())
//			offset = tpb;
//		else
//			offset = (this.batch) * window.getSlide(); // (this.batch - 1) * window.getSlide();
//		
//		System.out.println("[DBG] offset is " + offset);
//		
//		p = q = 0L;
//		next_ = 0L;
//		_next = tpb - 1;
//		
//		mask = buffer.capacity() - 1;
//		
//		tupleSize = schema.getByteSizeOfTuple();
//		
//		batches = new int [this.batchRecords][3];
//		/* Initialise state */
//		for (int i = 0; i < this.batchRecords; i++)
//			batches[i][_START] = _undefined;
//		b = d = 0;
//		
//		previous = -1;
//		
//		marks = new ArrayList<Integer>();
//		
//		nextBatchEndPointer = batchBytes;
//	}
//	
//	@Override
//	public void setup () {
//		/* The default task queue for either CPU or GPU executor */
//		this.workerQueue = this.parent.getExecutorQueue();
//	}
//	
//	@Override
//	public void dispatch (byte [] data, int length) {
//		int idx;
//		while ((idx = buffer.put(data, length)) < 0) {
//			Thread.yield();
//		}
//		assemble (idx, length);
//	}
//	
//	@Override
//	public boolean tryDispatch(byte [] data, int length) {
//		int idx;
//		if ((idx = buffer.put(data, length)) < 0) {
//			return false;
//		}
//		assemble (idx, length);
//		return true;
//	}
//	
//	private void newTaskFor (long p, long q, long free, long t_, long _t) {
//		Task task;
//		WindowBatch batch;
//		int taskid;
//		
//		taskid = this.getTaskNumber();
//		
////		if (p == q) {
////			q = p + tupleSize;
////		}
//		
//		 long size = (q <= p) ? (q + buffer.capacity()) - p : q - p;
//		  
////		  System.out.println(
////			String.format("[DBG] Query %d Task %6d [%10d, %10d), free %10d, [%6d, %6d] size %10d", 
////					parent.getId(), taskid, p, q, free, t_, _t, size)); 
//		 
//		if (q <= p) {
//			q += buffer.capacity();
//			// System.exit(1);
//		}
//		
//		/* Find latency mark */
//		int mark = -1;
//		if (Utils.LATENCY_ON) {
//			for (int i = 0; i < marks.size(); i++) {
//				if (marks.get(i) >= ((int) p) && marks.get(i) < ((int) free)) {
//					mark = marks.remove(i);
//					break;
//				}
//			}
//		}
//		
//		batch = WindowBatchFactory.newInstance(this.batch, taskid, (int) free, buffer, window, schema, mark);
//		if (window.isRangeBased()) {
//			if (getTimestamp(buffer, (int) p) > _t)
//				batch.cancel();
//			else
//				batch.setBatchPointers((int) p, (int) q);
//			batch.setRange(t_, _t);
//		} else {
//			batch.setBatchPointers((int) p, (int) q);
//			batch.setRange(t_, _t);
//		}
//		
//		/*
//		batch.initWindowPointers();
//		batch.debug();
//		*/
////		starttime = System.nanoTime();
//		
//		task = TaskFactory.newInstance(parent, batch, handler, taskid, (int) free);
//		
//		workerQueue.add(task);
//		
//		
////		count += 1;
////		if (count > 1) {
////			dt = (double) (System.nanoTime() - starttime);
////			if (count == 2) {
////				_m = m = dt;
////				_s = s = 0D;
////			} else {
////				m = _m + (dt - _m) / (count - 1);
////				s = _s + (dt - _m) * (dt - m);
////				_m = m;
////				_s = s;
////			}
////		}
////		starttime = System.nanoTime();
//	}
//	
//	private void assemble (int index, int length) {
//		
//		if (Utils.LATENCY_ON)
//			marks.add(index);
//		
//		/* Number of rows added */
//		int rows = length / tupleSize;
//		
//		int index_ = index - remainder;
//		
//		/* Consider length from index_, +length bytes */
//		int _length = (int) Math.floor((double) (length + remainder) / (double) tupleSize) * tupleSize;
//		remainder = (length + remainder) - _length;
//		
//		/* Index of the last tuple inserted in the circular buffer */
//		int _index = (int) ((index_ + _length - tupleSize) & mask);
//		
////		System.out.println(String.format("[DBG] start %16d end %16d index %10d _index %10d length %10d next_ %16d rows %16d", 
////				start, end, index_, _index, length, next_, rowCount));
//		
////		if (window.isRowBased()) {
////			while ((rowCount + rows) >= _next + 1) {
////				/* Set start and end pointers for batch */
////				p = ((next_) * tupleSize) & mask;
////				q = ((_next + 1) * tupleSize) & mask;
////				q = (q == 0) ? buffer.capacity() : q;
////				/* Set free pointer */
////				f = (p + (offset * tupleSize)) & mask;
////				f = (f == 0) ? buffer.capacity() : f;
////				f--;
////				/* Dispatch task */
////				this.newTaskFor (p, q, f, _undefined, _undefined);
////				if (window.isTumbling()) {
////					next_ += offset;
////					_next += tpb;
////				}
////				else {
////					next_ += offset;
////					_next += ((this.batch) * window.getSlide()); // ((this.batch - 1) * window.getSlide());
////				}
////			}
////		} else
//		if (window.isRangeBased() || window.isRowBased()) {
//			/* Get the timestamp of the first and last tuple inserted */
////N			start = getTimestamp(buffer, index_);
////N			end   = getTimestamp(buffer, _index);
//			
////			if (first) {
////				next_  = start;
////				_next += next_;
////				first = false;
////			}
//			
//			/*
//			 * Inserted `length` bytes, from `index_` to `_index`.
//			 * 
//			 * The first tuple timestamp is `start` and the last tuple timestamp is `end`.
//			 * 
//			 */
//			// System.out.println(String.format("[DBG] range-based window: start %16d end %16d index %10d _index %10d length %10d", 
//			//		start, end, index_, _index, length));
//			
//			if (first) {
//				// batches[b][_START] = 0;
//				thisBatchStartPointer = 0;
//				nextBatchEndPointer = batchBytes;
//				batchStartTimestamp = 0;
//				first = false;
//			}
//			
//			accumulated += length;
//			
//			// System.out.println(String.format("[DBG] %20d bytes accumulated; next batch end pointer is %10d", accumulated, nextBatchEndPointer));
//			
//			/* Open and close one or more window batches */
//			
//			while (accumulated >= nextBatchEndPointer) {
//				
//				/* Launch task */
//				this.newTaskFor (
//					thisBatchStartPointer & mask, 
//					nextBatchEndPointer & mask, 
//					nextBatchEndPointer & mask, 
//					thisBatchStartPointer, nextBatchEndPointer
//					);
//				
//				// b = incrementAndGet(b, true);
//				// batches[b][_START] = (int) ((nextBatchStartPointer) & mask);
//				
//				thisBatchStartPointer = thisBatchStartPointer + batchBytes;
//				nextBatchEndPointer = nextBatchEndPointer + batchBytes;
//				
//				// batchStartTimestamp = endTimestamp;
//				
//				
//				
//				// We have enough data for a batch
//				// Close the current batch
//				// The end pointer is the start pointer + batchBytes
//				// What is the timestamp of the tuple at `endPointer`?
////N				long endTimestamp = getTimestamp (buffer, (int) ((nextBatchEndPointer) & mask));
//				// System.out.println(String.format("[DBG] end timestamp is %10d; end batch at %10d", endTimestamp, endTimestamp - window.getSlide()));
//				// We cannot include the current timestamp,
//				// because it belongs to a window that may not
//				// be closed yet.
//				// We  go one before.
////N				endTimestamp -= window.getSlide();
//				// Look backwards for the last tuple with timestamp `endTimestamp` or less.
////N				while (nextBatchEndPointer >= thisBatchStartPointer && 
////N						getTimestamp (buffer, (int) ((nextBatchEndPointer) & mask)) > endTimestamp) {
//					// System.out.println(String.format("[DBG] batch end pointer is %13d...", nextBatchEndPointer));
////N					nextBatchEndPointer -= tupleSize;
////N				}
////N				nextBatchEndPointer += tupleSize;
//				
////N				thisBatchEndPointer = (int) ((nextBatchEndPointer) & mask);
//				
//				// OK. So we have a batch end pointer and a batch start pointer.
//				// Can we dispatch a task?
//				// What's missing is a free pointer. The free pointer is the
//				// beginning of the next batch.
//				// The next pointer is the beginning of window with timestamp `endTimestamp + slide`.
////				nextBatchStartPointer = nextBatchEndPointer;
////				while (getTimestamp (buffer, (int) ((nextBatchStartPointer) & mask)) >= endTimestamp) {
//////					System.out.println(String.format("[DBG] next batch start pointer is %13d with timestamp %5d...", 
//////							nextBatchStartPointer, getTimestamp (buffer, (int) ((nextBatchStartPointer) & mask))));
////					nextBatchStartPointer -= tupleSize;
////				}
////				nextBatchStartPointer += tupleSize;
////				thisBatchFreePointer = (int) ((nextBatchStartPointer) & mask);
////				
////				/* Launch task */
////				this.newTaskFor (
////					thisBatchStartPointer & mask, 
////					thisBatchEndPointer & mask, 
////					thisBatchFreePointer & mask, 
////					batchStartTimestamp, endTimestamp
////					);
////				batches[b][_START] = _undefined;
////				
////				b = incrementAndGet(b, true);
////				batches[b][_START] = (int) ((nextBatchStartPointer) & mask);
////				
////				nextBatchEndPointer = nextBatchStartPointer + batchBytes;
////				thisBatchStartPointer = nextBatchStartPointer;
////				
////				batchStartTimestamp = endTimestamp;
//			}
//			
////			/* Open one or more window batches */
////			current = 0;
////			while (end >= next_) {
////				/* Let's assume that we insert a sequence a tuples, marked 'x'
////				 * whose timestamps are 1, 2, 2, and 8.
////				 * 
////				 * The slide of the window is 1, so we wish to open windows at
////				 * times 1, 2, 3, and so on.
////				 * 
////				 * For t = 1 and t = 2, everything is fine. For t = 3, though,
////				 * there is no tuple with such timestamp.
////				 * 
////				 * When does the batch open? It may open at time 4, 5, 6...So,
////				 * one way is to search for t + 1, t + 2, and so on, until we
////				 * find the next tuple with a timestamp greater than t = 3.
////				 * 
////				 * x---xx-----------------x---------------------> t
////				 * 
////				 * |--|--|--|--|--|--|
////				 * 1                 7
////				 *    |--|--|--|--|--|--|
////				 *    2                 8
////				 *       |--|--|--|--|--|--|
////				 *       3                 9
////				 */
////				tmp = next_;
////				while ((position = firstOccurenceOf (tmp, current, rows, index_)) < 0)
////					tmp ++;
////				
////				/* Set the start pointer for this window batch */
////				batches[b][_START] = position;
////				
////				/* 
////				 * Set the free pointer for the previous window batch, if any. 
////				 * If position is 0, then the free pointer should point at the 
////				 * end of the buffer, otherwise `position--` will be negative.
////				 */
////				position = (position == 0) ? buffer.capacity() : position;
////				position --;
////				if (previous >= 0)
////					batches[previous][_FREE] = position;
////				/* Set counters for the next batch to open */
////				previous = b;
////				b = incrementAndGet(b, true);
////				next_ += offset;
////			}
////			/* Should we close old batches? 
////			 * 
////			 * If a window batch close at time `t`, we are looking for the
////			 * first element whose timestamp is greater than `t`.
////			 *  
////			 */
////			current = 0;
////			while (end >= (_next + 1)) {
////				
////				tmp = _next + 1;
////				while ((position = firstOccurenceOf (tmp, current, rows, index_)) < 0)
////					tmp ++;
////				
////				/* Set the end pointer for this window batch */
////				batches[d][_END] = position;
////				
////				if (batches[d][_FREE] == buffer.capacity()) {
////					System.out.println("Free buffer");
////				}
////				
////				/* Dispatch a task */
////				this.newTaskFor (
////						batches[d][_START], 
////						batches[d][  _END], 
////						batches[d][ _FREE], 
////						_next - tpb + 1, _next
////						);
////				batches[d][_START] = _undefined;
////				/* Set counters for the next batch to close */
////				d = incrementAndGet(d, false);
////				_next += offset;
////			}
//		} else
//		{
//			throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
//		}
//		rowCount += rows;
//	}
//	
//	private int incrementAndGet (int x, boolean check) {
//		int value = ++x & (this.batchRecords - 1);
//		/* We treat `batches` as an unsafe circular buffer. But, without 
//		 * sufficient capacity, we may ovewrite the pointers of a window 
//		 * batch that is currently open.
//		 */
//		if (check)
//			if (batches[value][_START] >= 0)
//				throw new IllegalStateException(String.format("error: batch %d is currently open", value));
//		return value;
//	}
//	
//	
//	private int firstOccurenceOf (long t, int start, int end, int offset) {
//		int position;
//		if ((position = binarySearch(t, start, end, offset)) < 0)
//			return -1;
//		return scanLeft (t, position, offset);
//	}
//	
//	private int scanLeft (long t, int index, int offset) {
//		/*
//		 * The `index` points to the location in the byte buffer of a tuple 
//		 * whose timestamp is `t`. But, is this tuple the first one?
//		 */
//		
//		while (index >= offset && getTimestamp (buffer, index) == t) {
//			index -= tupleSize;
//		}
//		index += tupleSize;
//		return index;
//	}
//
//	private int binarySearch (long t, int start, int end, int offset) {
//		/*
//		 * The `start` and `end` pointers represent tuple and not byte indices
//		 */
//		while (start <= end) {
//			int m = start + (end - start) / 2;
//			/* Normalize tuple offset in byte buffer */
//			int y = (int) ((offset + m * tupleSize) & mask);
//			if (t < getTimestamp (buffer, y)) 
//				end = m - 1;
//			else 
//			if (t > getTimestamp (buffer, y))
//				start = m + 1;
//			else {
//				this.current = m;
//				return y;
//			}
//		}
//		return -1;
//	}
//	
//	private int getTaskNumber () {
//		int id = nextTask ++;
//		if (nextTask == Integer.MAX_VALUE)
//			nextTask = 1;
//		return id;
//	}
//	
//	@Override
//	public IQueryBuffer getBuffer () {
//		return this.buffer;
//	}
//
//	@Override
//	public void dispatchSecond (byte [] data, int length) {
//		throw new UnsupportedOperationException("Cannot dispatch to second buffer since this is a single-input dispatcher");
//	}
//
//	@Override
//	public IQueryBuffer getSecondBuffer() {
//		/* throw new UnsupportedOperationException("Cannot get second buffer since this is a single-input dispatcher"); */
//		return null;
//	}
//
//	@Override
//	public boolean tryDispatchFirst(byte[] data, int length) {
//		
//		return tryDispatch (data, length);
//	}
//
//	@Override
//	public boolean tryDispatchSecond(byte[] data, int length) {
//		
//		return tryDispatch (data, length);
//	}
//	
//	private long getTimestamp (IQueryBuffer buffer, int index) {
//		long value = buffer.getLong(index);
//		if (Utils.LATENCY_ON)
//			return (long) Utils.unpack(1, value);
//		else 
//			return value;
//	}
//
//	@Override
//	public long getBytesGenerated() {
//		
//		return handler.getTotalOutputBytes();
//	}
//	
//	public int getWindowStateSize () {
//		
//		// return handler.windowResults.next;
//		return 0; // (int) handler.windowResults.size();
//	}
//	
//	public ResultHandler getHandler () {
//		return handler;
//	}
//	
//	public double mean () {
//		avg = (count > 0) ? m : 0D;
//		return avg;
//	}
//	
//	public double stdv () {
//		std = (count > 2) ? Math.sqrt(s / (double) (count - 1 - 1)) : 0D;
//		return std;
//	}
}

