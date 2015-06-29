package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class WindowBatch {
	
	private int batchSize;
	
	private int numWindowsInBatch;
	
	private IQueryBuffer buffer;
	private WindowDefinition windowDefinition;
	private ITupleSchema schema;
	
	private int taskId;
	private int freeOffset;
	
	private int bufferStartPointer;
	private int bufferEndPointer;
	
	private long batchStartPointer;
	private long batchEndPointer;
	
	private int [] windowStartPointers;
	private int [] windowEndPointers;
	
	private boolean initialised = false;
	
	private long batchStartTime;
	private long batchEndTime;
	
	private int prevWindowStartPointer;
	private int prevWindowEndPointer;
	
	private int latencyMark = 0;
	
	private boolean partialResults = false;
	private boolean hasPending = false;
	private int lastWindowIndex = 0;
	
	PartialWindowResults opening, closing, pending, complete;
	
	public WindowBatch () {
		this(0, 0, 0, null, null, null, 0);
	}
	
	public WindowBatch (int batchSize, 
                        int taskId,
                        int freeOffset,
                        IQueryBuffer buffer, 
                        WindowDefinition windowDefinition, 
                        ITupleSchema schema,
                        int latencyMark) {
		
		this.batchSize = batchSize;
		this.numWindowsInBatch = -1; /* Unknown */
		
		this.taskId = taskId;
		this.freeOffset = freeOffset;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		
		this.latencyMark = latencyMark;
		
		this.bufferStartPointer = -1;
		this.bufferEndPointer = -1;
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		
		this.windowStartPointers = new int [65536 * 2];
		this.windowEndPointers   = new int [65536 * 2];
		
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
		
		this.prevWindowStartPointer = -1;
		this.prevWindowEndPointer = -1;
		
		this.closing = null;
		this.pending = null;
		this.complete = null;
		this.opening = null;
		
		this.partialResults = false;
		this.hasPending = false;
		
		this.lastWindowIndex = 0;
	}
	
	public void set (int batchSize, 
                     int taskId,
                     int freeOffset,
                     IQueryBuffer buffer, 
                     WindowDefinition windowDefinition, 
                     ITupleSchema schema,
                     int latencyMark) {
		
		this.batchSize = batchSize;
		this.taskId = taskId;
		this.freeOffset = freeOffset;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		
		this.bufferStartPointer = -1;
		this.bufferEndPointer = -1;
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		
		/*
		 * Avoid the cost of initialising
		 * the arrays.
		 * 
		Arrays.fill(windowStartPointers, -1);
		Arrays.fill(  windowEndPointers, -1);
		*/
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
		
		this.latencyMark = latencyMark;
		
		this.prevWindowStartPointer = -1;
		this.prevWindowEndPointer = -1;
		
		this.closing = null;
		this.pending = null;
		this.complete = null;
		this.opening = null;
		
		this.partialResults = false;
		this.hasPending = false;
		
		this.lastWindowIndex = 0;
	}
	
	public void setTaskId (int taskId) {
		this.taskId = taskId;
	}
	
	public int getTaskId () {
		return this.taskId;
	}
	
	public int getFreeOffset () {
		return this.freeOffset;
	}
	
	public void setFreeOffset (int freeOffset) {
		this.freeOffset = freeOffset;
	}
	
	public void setBufferPointers (int bufferStartPointer, int bufferEndPointer) {
		this.bufferStartPointer = bufferStartPointer;
		this.bufferEndPointer = bufferEndPointer;
	}
	
	public void cancel () {
		this.bufferStartPointer = -1;
		this.bufferEndPointer = -1;
	}
	
	public boolean isEmpty () {
		return (this.bufferStartPointer == -1) && (this.bufferEndPointer == -1);
	}
	
	public int getBatchSize () {
		return this.batchSize;
	}
	
	public void setBatchTime (long batchStartTime, long batchEndTime) {
		
		this.batchStartTime = unpackTimestamp(batchStartTime);
		this.batchEndTime   = unpackTimestamp(batchEndTime);
	}
	
	private static int __indexOf (int idx) { return 4 * idx; }
	
	public void initWindowPointers (byte [] startPtrs, byte [] endPtrs) {
		
		if (bufferStartPointer < 0 && bufferEndPointer < 0)
			return ;
		
		if (numWindowsInBatch < 0)
			return ;
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		
		int tuple_ = schema.getByteSizeOfTuple ();
		int window_ = (int) windowDefinition.getSize();
		int slide_ = (int) windowDefinition.getSlide();
		
		if (windowDefinition.isRowBased()) {
			
			/* Bytes/window */
			int bpw = tuple_ * window_;
			
			int offset  = schema.getByteSizeOfTuple (); /* In bytes */
			if (windowDefinition.isTumbling())
				offset *= window_;
			else
				offset *= slide_;
			
			b.putInt(bufferStartPointer - bufferStartPointer);
			d.putInt(bufferStartPointer + bpw - bufferStartPointer);
			
			for (int i = 1; i < numWindowsInBatch; i++) {
				b.putInt(b.getInt((i-1) * 4) + offset);
				d.putInt(d.getInt((i-1) * 4) + offset);
			}
		} else { /* Fill-in range-based windows */
			int p = 0; /* Current opened window */ 
			int q = 0; /* Current closed window */
			
			b.putInt(__indexOf(p), 0);
			
			for (int i = bufferStartPointer; i <= bufferEndPointer; i += tuple_) {
				long t = getTimestamp(i);
				/* 
				 * Should we open new windows? 
				 */
				boolean open = false;
				while (t - slide_ >= this.batchStartTime + ((long) p) * windowDefinition.getSlide()) {
					p ++;
					open |= true;
				}
				if (open && p < this.numWindowsInBatch)
					b.putInt(__indexOf(p), i - this.bufferStartPointer);
				/* 
				 * Should we close old windows? 
				 */
				boolean close = true;
				
				while (t > this.batchStartTime + q * windowDefinition.getSlide() + windowDefinition.getSize() - 1) {
					if (close)
						d.putInt(__indexOf(q), i - this.bufferStartPointer);
					close = false;
					q ++;
				}
			} /* End of batch */
		}
	}
	
	public void initWindowPointers () {
		
		if (numWindowsInBatch < 0)
			return;
		
		windowStartPointers = new int [numWindowsInBatch];
		windowEndPointers   = new int [numWindowsInBatch];
		
		Arrays.fill(windowStartPointers, -1);
		Arrays.fill(windowEndPointers,   -1);
		
		if (bufferStartPointer < 0 && bufferEndPointer < 0)
			return ;
		
		int tuple_ = schema.getByteSizeOfTuple ();
		int window_ = (int) windowDefinition.getSize();
		int slide_ = (int) windowDefinition.getSlide();
		
		if (windowDefinition.isRowBased()) {
			
			/* Bytes/window */
			int bpw = tuple_ * window_;
			
			int offset  = schema.getByteSizeOfTuple (); /* In bytes */
			if (windowDefinition.isTumbling())
				offset *= window_;
			else
				offset *= slide_;
			
			windowStartPointers [0] = bufferStartPointer;
			windowEndPointers   [0] = windowStartPointers[0] + bpw;
			
			for (int i = 1; i < numWindowsInBatch; i++) {
				windowStartPointers [i] = windowStartPointers [i - 1] + offset;
				windowEndPointers   [i] = windowEndPointers   [i - 1] + offset;
			}
		} else { /* Fill-in range-based windows */
			
			int p = 0; /* Current opened window */ 
			int q = 0; /* Current closed window */
			
			this.windowStartPointers[p] = this.bufferStartPointer;
			
			for (int i = bufferStartPointer; i <= bufferEndPointer; i += tuple_) {
				long t = getTimestamp(i);
				/* 
				 * Should we open new windows? 
				 */
				boolean open = false;
				while (t - slide_ >= this.batchStartTime + ((long) p) * windowDefinition.getSlide()) {
					p ++;
					open |= true;
				}
				if (open && p < this.numWindowsInBatch)
					this.windowStartPointers[p] = i;
				/* 
				 * Should we close old windows? 
				 */
				boolean close = true;
				
				while (t > this.batchStartTime + q * windowDefinition.getSlide() + windowDefinition.getSize() ) {
					if (close)
						this.windowEndPointers[q] = i;
					close = false;
					q ++;
				}
			} /* End of batch */
		}
	}
	
	public void initPrevWindowPointers () {
		
		/* Find start and end pointer of the last window
		 * from the previous batch.
		 */
		if (bufferStartPointer < 0 && bufferEndPointer < 0)
			return ;
		
		if (taskId == 0)
			return ;
		
		int tuple_  = schema.getByteSizeOfTuple ();
		int window_ = (int) windowDefinition.getSize();
		int slide_  = (int) windowDefinition.getSlide();
		
		int idx;
		
		if (windowDefinition.isRowBased()) {
			
			/* Bytes/window */
			int bpw = tuple_ * window_;
			
			int offset  = schema.getByteSizeOfTuple (); /* In bytes */
			if (windowDefinition.isTumbling())
				offset *= window_;
			else
				offset *= slide_;
			
			this.prevWindowStartPointer = this.bufferStartPointer - offset;
			// check whether we crossed the buffer boundary
			if (this.prevWindowStartPointer < 0)
				this.prevWindowStartPointer += buffer.capacity();
			
			this.prevWindowEndPointer = this.prevWindowStartPointer + bpw; 
			
		} else { /* Find last range-based window */
			
			long previousStartTime = this.batchStartTime - windowDefinition.getSlide();
			/* We work our way backwards until we find the first tuple
			 * whose timestamp is less than `previousStartTime` 
			 */
			idx = bufferStartPointer;
			long t = getTimestamp(idx);
			while (t >= previousStartTime) {
				idx -= tuple_;
				if (idx < 0)
					idx += buffer.capacity();
				t = getTimestamp(idx);
			}
			this.prevWindowStartPointer = idx + tuple_;
			
			long previousEndTime = previousStartTime + windowDefinition.getSize();
			/* We work our way forward until we find the first tuple
			 * whose timestamp is greater than `previousEndTime` 
			 */
			idx = prevWindowStartPointer;
			t = getTimestamp(idx);
			while (t < previousEndTime) {
				idx += tuple_;
				t = getTimestamp(idx);
			}
			this.prevWindowEndPointer = idx;
		}
		
		System.out.println(String.format("[DBG] last window of previous batch starts %10d ends %10d",
				this.prevWindowStartPointer, this.prevWindowEndPointer));
	}
	
	public void moveFreePointerToNotFreeLastWindow () {
		/*
		 * Set new free pointer to the byte before the start of the last 
		 * window of this window batch
		 */
		this.freeOffset = this.windowStartPointers[this.windowStartPointers.length - 1] - 1;
		/* Check whether we need to wrap */
		this.freeOffset = (this.freeOffset < 0) ? this.freeOffset + buffer.capacity() : this.freeOffset;
	}
	
	public void clear () {
		initialised = false;
		/* windowStartPointers = windowEndPointers = null; */
		batchStartTime = batchEndTime = -1;
		this.buffer = null;
		this.prevWindowStartPointer = this.prevWindowEndPointer = -1;
	}
	
	public int getInt (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getInt (index);
	}
	
	public long getLong (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getLong (index);
	}
	
	public float getFloat (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getFloat (index);
	}
	
	public void putInt (int value) {
		this.buffer.putInt (value);
	}
	
	public void putLong (long value) {
		this.buffer.putLong (value);
	}
	
	public void putFloat (float value) {
		this.buffer.putFloat (value);
	}
	
	public int [] getWindowStartPointers () {
		return this.windowStartPointers;
	}
	
	public int [] getWindowEndPointers () {
		return this.windowEndPointers;
	}
	
	public int getBufferStartPointer () {
		return this.bufferStartPointer;
	}
	
	public int getBufferEndPointer () {
		return this.bufferEndPointer;
	}
	
	public long getBatchStartTime () {
		return this.batchStartTime;
	}
	
	public long getBatchEndTime () {
		return this.batchEndTime;
	}
	
	public void setBuffer (IQueryBuffer buffer) {
		this.buffer = buffer;
	}
	
	public IQueryBuffer getBuffer () {
		return this.buffer;
	}
	
	public ITupleSchema getSchema () {
		return this.schema;
	}
		
	public WindowDefinition getWindowDefinition () {
		return this.windowDefinition;
	}
	
	public void setSchema (ITupleSchema schema) {
		this.schema = schema;
	}
	
	/* 
	 * Print statistics for every window in batch 
	 */
	public void debug () {
		
		for (int i = 0; i < this.numWindowsInBatch; i++) {
			
			int start = this.windowStartPointers[i];
			int end = this.windowEndPointers[i];
			int bytes = end - start;
			int tuples = bytes / schema.getByteSizeOfTuple();
			
			System.out.println(String.format("[DBG] window %3d starts at %10d ends at %10d %10d bytes %10d tuples", 
					i, start, end, bytes, tuples));
		}
	}

	public void setWindowStartPointers (int [] windowStartPointers) {
		this.windowStartPointers = windowStartPointers;
	}

	public void setWindowEndPointers (int [] windowEndPointers) {
		this.windowEndPointers = windowEndPointers;
	}

	public void normalizeWindowPointers () {
		
		for (int i = 0; i < numWindowsInBatch; i++) {
			windowStartPointers[i] -= bufferStartPointer;
			windowEndPointers  [i] -= bufferStartPointer;
		}
	}
	
	public void normalizeWindowPointers (int [] startPtrs, int [] endPtrs) {
		
		for (int i = 0; i < numWindowsInBatch; i++) {
			startPtrs[i] -= bufferStartPointer;
			endPtrs  [i] -= bufferStartPointer;
		}
	}

	public int getPrevWindowStartPointer() {
		return prevWindowStartPointer;
	}

	public int getPrevWindowEndPointer() {
		return prevWindowEndPointer;
	}

	public int getLatencyMark() {
		return this.latencyMark;
	}
	
	public void setLatencyMark(int latencyMark) {
		this.latencyMark  = latencyMark;
	}
	
	/*
	 * Normalize a pointer to a location of the 
	 * underlying byte buffer.
	 * 
	 * Avoids "out of bounds" memory accesses,
	 * especially when we copy memory via the
	 * Unsafe interface.
	 */
	public int normalise (int pointer) {
		
		return this.buffer.normalise((long) pointer);
	}
	
	public PartialWindowResults getOpening() {
		return opening;
	}
	
	public void setOpening(PartialWindowResults opening) {
		this.partialResults = true;
		this.opening = opening;
	}

	public PartialWindowResults getClosing() {
		return closing;
	}

	public void setClosing(PartialWindowResults closing) {
		this.partialResults = true;
		this.closing = closing;
	}

	public PartialWindowResults getPending() {
		return pending;
	}

	public void setPending(PartialWindowResults pending) {
		this.partialResults = true;
		this.pending = pending;
	}

	public PartialWindowResults getComplete() {
		return complete;
	}

	public void setComplete(PartialWindowResults complete) {
		this.partialResults = true;
		this.complete = complete;
	}
	
	private long getTimestamp (int index) {
		long value = this.buffer.getLong(index);
		if (Utils.LATENCY_ON)
			return (long) Utils.unpack(1, value);
		else 
			return value;
	}
	
	private long unpackTimestamp (long timestamp) {
		if (Utils.LATENCY_ON)
			return (long) Utils.unpack(1, timestamp);
		else 
			return timestamp;
	}

	public void setBatchPointers(long batchStartPointer, long batchEndPointer) {
		this.batchStartPointer = batchStartPointer;
		this.batchEndPointer   = batchEndPointer;
	}
	
	public long getBatchStartPointer () {
		return this.batchStartPointer;
	}
	
	public long getBatchEndPointer () {
		return this.batchEndPointer;
	}
	
	public boolean hasPartialResults () {
		return this.partialResults;
	}
	
	public boolean hasPending () {
		return this.hasPending;
	}
	
	public int getLastWindowIndex () {
		return lastWindowIndex;
	}
	
	public void initPartialWindowPointers () {
		
		if (windowDefinition.isRangeBased()) {
			initPartialRangeBasedWindowPointers ();
		} else {
			initPartialCountBasedWindowPointers ();
		}
	}
	
	private static final boolean debug = true;
	
	public void initPartialRangeBasedWindowPointers () {
		
		int tupleSize = schema.getByteSizeOfTuple ();
		long paneSize = windowDefinition.getPaneSize(); 
		
		Arrays.fill(windowStartPointers, -1);
		Arrays.fill(  windowEndPointers, -1);
		
		long streamIndex;
		int bufferIndex;
		long _pid, pid_, pid = 0;
		
		long normalisedPaneId; /* Normalised to incl. panes/window */
		
		/* Set previous pane id */
		if (this.batchStartPointer == 0) {
			_pid = -1;
		} else {
			/* Check the last tuple of the previous batch */
			_pid = (getTimestamp(bufferStartPointer - schema.getByteSizeOfTuple()) / paneSize);
		}
		
		long offset = -1; /* Undefined */
		if (this.batchStartPointer == 0) {
			offset = 0;
		}
		
		long wid;
		
		for (streamIndex = batchStartPointer, bufferIndex = bufferStartPointer; 
				streamIndex < batchEndPointer && bufferIndex < bufferEndPointer; 
				streamIndex += tupleSize, bufferIndex += tupleSize) {
			
			pid = getTimestamp(bufferIndex) / paneSize; /* Current pane */
			
			if (debug)
				System.out.println(String.format("[DBG] previous pane %6d current pane %6d", _pid, pid));
			
			if (_pid < pid) {
				/* Pane `_pid` closed; pane `pid` opened */
				while (_pid < pid) {
					
					pid_ = _pid + 1;
					
					/* Check if a window closes at this pane */
					normalisedPaneId = pid_ - windowDefinition.numberOfPanes();
					
					if (normalisedPaneId >= 0 && normalisedPaneId % windowDefinition.panesPerSlide() == 0) {
						
						wid = normalisedPaneId / windowDefinition.panesPerSlide();
						
						if (wid >= 0) {
							
							if (debug)
								System.out.println(String.format("[DBG] closing %05d at pane %10d buffer index %10d", 
										wid, pid_, bufferIndex));
							
							/* Calculate offset */
							if (offset < 0) {
								offset = wid;
								/* System.out.println(String.format("[DBG] window %05d is closing; offset %10d", wid, offset)); */
							}
							
							/* Store end pointer */
							int index = (int) (wid - offset);
							windowEndPointers[index] = (int) bufferIndex;
							
							/*
							 * Has this window been previously opened? If the start pointer is -1,
							 * then the window processing starts from the beginning of the batch.
							 * 
							 * We characterise this window as "closing" and we expect to find its
							 * match in the opening set of the previous batch. But if this is the
							 * first batch, then there will be none.
							 */
							if (windowStartPointers[index] < 0 && batchStartPointer == 0)
								windowStartPointers[index] = 0;
							
							lastWindowIndex = (lastWindowIndex < index) ? index : lastWindowIndex;
						}
					}
					
					/* Check if a window opens at `pid_` */
					if (pid_ % windowDefinition.panesPerSlide() == 0) {
					
						wid = pid_ / windowDefinition.panesPerSlide();
						
						if (debug)
							System.out.println(String.format("[DBG] opening %05d at pane %10d buffer index %10d", 
									wid, pid_, bufferIndex));
						
						/* Calculate offset */
						
						if (offset < 0) {
							offset = wid - 1;
							/* System.out.println(String.format("[DBG] window %05d is opening; offset %10d", wid, offset)); */
						}
						
						
						/* Store start pointer */
						int index;
						if (offset < 0) {
							index = (int) wid;
						} else {
							index = (int) (wid - offset);
						}
						windowStartPointers[index] = (int) bufferIndex;
						lastWindowIndex = (lastWindowIndex < index) ? index : lastWindowIndex;
					}
					_pid += 1;
				}
				_pid = pid;
			}
		} /* End for */
	}
	
	public void initPartialCountBasedWindowPointers () {
		
		int tupleSize = schema.getByteSizeOfTuple ();
		long paneSize = windowDefinition.getPaneSize(); 
		
		Arrays.fill(windowStartPointers, -1);
		Arrays.fill(  windowEndPointers, -1);
		
		long streamIndex, bufferIndex;
		long pid = 0;
		long _pid = ((batchStartPointer / tupleSize) / paneSize) - 1; /* _pid is the previous pane id */
		long __pid; /* Normalised to incl. panes/window */
		
		long offset = -1; /* Undefined */
		
		long wid;
		
		for (streamIndex = batchStartPointer, bufferIndex = bufferStartPointer; 
				streamIndex < batchEndPointer && bufferIndex < bufferEndPointer; 
				streamIndex += tupleSize, bufferIndex += tupleSize) {
			
			pid = (streamIndex / tupleSize) / paneSize; /* Current pane */
			
			if (_pid < pid) {
				
				/* Pane `_pid` closed; pane `pid` opened */
				
				/* Check if a window closes at `_pid` */
				
				/* Check if a window closes at `_pid` */
				__pid =  _pid - windowDefinition.numberOfPanes();
				if (__pid >= 0 && __pid % windowDefinition.panesPerSlide() == 0) {
					
					wid = __pid / windowDefinition.panesPerSlide();
					
					if (wid >= 0) {
						
						System.out.println(String.format("[DBG] closing %05d; buffer index %10d", 
						wid, bufferIndex));
						
						
						/* Calculate offset */
						if (offset < 0) {
							offset = wid;
							/* System.out.println(String.format("[DBG] window %05d is closing; offset %10d", 
							 * wid, offset)); */
						}
						
						/* Store end pointer */
						int index = (int) (wid - offset);
						windowEndPointers[index] = (int) bufferIndex;
						lastWindowIndex = (lastWindowIndex < index) ? index : lastWindowIndex;
					}
				}
				/* Check if a window opens at `pid` */
				if ( pid % windowDefinition.panesPerSlide() == 0) {
					
					wid = pid / windowDefinition.panesPerSlide();
					/*
					System.out.println(String.format("[DBG] opening %05d; buffer index %10d", 
					wid, bufferIndex));
					*/
					
					/* Store start pointer */
					int index;
					if (offset < 0) {
						index = (int) wid;
					} else {
						index = (int) (wid - offset);
					}
					windowStartPointers[index] = (int) bufferIndex;
					lastWindowIndex = (lastWindowIndex < index) ? index : lastWindowIndex;
				}
				_pid = pid;
			}
		} /* End for */
	}
}
