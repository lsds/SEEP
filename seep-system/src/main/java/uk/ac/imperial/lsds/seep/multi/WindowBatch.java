package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class WindowBatch {
	
	private int batchSize;
	
	private IQueryBuffer buffer;
	private WindowDefinition windowDefinition;
	private ITupleSchema schema;
	
	private int taskId;
	private int freeOffset;
	
	private int batchStartPointer;
	private int batchEndPointer;
	
	private int [] windowStartPointers;
	private int [] windowEndPointers;
	
	private boolean initialised = false;
	
	/* Expected timestamps, based on range and slide of window definition */
	private long batchStartTime;
	private long batchEndTime;
	
	private int prevStartPointer;
	private int prevEndPointer;

	private int latencyMark = 0;
	
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
		this.taskId = taskId;
		this.freeOffset = freeOffset;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		
		this.latencyMark = latencyMark;
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		
		this.windowStartPointers = null;
		this.windowEndPointers = null;
		
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
		
		this.prevStartPointer = -1;
		this.prevEndPointer = -1;
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
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
		
		this.latencyMark = latencyMark;
		
		this.prevStartPointer = -1;
		this.prevEndPointer = -1;
	}
	
	public void setTaskId (int taskId) {
		this.taskId = taskId;
	}
	
	public int getTaskId () {
		return this.taskId;
	}

	public int getFreeOffset () {
		return freeOffset;
	}
	
	public void setFreeOffset (int freeOffset) {
		this.freeOffset = freeOffset;
	}
	
	public void setBatchPointers (int batchStartPointer, int batchEndPointer) {
		this.batchStartPointer = batchStartPointer;
		this.batchEndPointer = batchEndPointer;
	}
	
	public void cancel () {
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
	}
	
	public boolean isEmpty () {
		return (this.batchStartPointer == -1) && (this.batchEndPointer == -1);
	}
	
	public int getBatchSize () {
		return this.batchSize;
	}
	
	public void setRange (long batchStartTime, long batchEndTime) {
		this.batchStartTime = unpackTimestamp(batchStartTime);
		this.batchEndTime   = unpackTimestamp(batchEndTime);
	}
	
	private static int __indexOf (int idx) { return 4 * idx; }
	
	public void initWindowPointers (byte [] startPtrs, byte [] endPtrs) {
		
		if (batchStartPointer < 0 && batchEndPointer < 0)
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
			
			b.putInt(batchStartPointer - batchStartPointer);
			d.putInt(batchStartPointer + bpw - batchStartPointer);
			
			for (int i = 1; i < batchSize; i++) {
				b.putInt(b.getInt((i-1) * 4) + offset);
				d.putInt(d.getInt((i-1) * 4) + offset);
			}
		} else { /* Fill-in range-based windows */
			int p = 0; /* Current opened window */ 
			int q = 0; /* Current closed window */
			
			b.putInt(__indexOf(p), 0);
			
			for (int i = batchStartPointer; i <= batchEndPointer; i += tuple_) {
				long t = getTimestamp(i);
				/* 
				 * Should we open new windows? 
				 */
				boolean open = false;
				while (t - slide_ >= this.batchStartTime + ((long) p) * windowDefinition.getSlide()) {
					p ++;
					open |= true;
				}
				if (open && p < this.batchSize)
					b.putInt(__indexOf(p), i - this.batchStartPointer);
				/* 
				 * Should we close old windows? 
				 */
				boolean close = true;
				
				while (t > this.batchStartTime + q * windowDefinition.getSlide() + windowDefinition.getSize() - 1) {
					if (close)
						d.putInt(__indexOf(q), i - this.batchStartPointer);
					close = false;
					q ++;
				}
			} /* End of batch */
		}
	}
	
	public void initWindowPointers () {
		
		// System.out.println("[DBG] batch size is " + batchSize);
		windowStartPointers = new int [batchSize];
		windowEndPointers   = new int [batchSize];
		
		Arrays.fill(windowStartPointers, -1);
		Arrays.fill(windowEndPointers,   -1);
		
		if (batchStartPointer < 0 && batchEndPointer < 0)
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
			
			windowStartPointers [0] = batchStartPointer;
			windowEndPointers   [0] = windowStartPointers[0] + bpw;
			
			for (int i = 1; i < batchSize; i++) {
				windowStartPointers [i] = windowStartPointers [i - 1] + offset;
				windowEndPointers   [i] = windowEndPointers   [i - 1] + offset;
			}
		} else { /* Fill-in range-based windows */
			
			int p = 0; /* Current opened window */ 
			int q = 0; /* Current closed window */
			
			this.windowStartPointers[p] = this.batchStartPointer;
			
			/* HACK - debugging real-time and a window of 1 */
			this.windowEndPointers[q] = this.batchEndPointer;
			
//			for (int i = batchStartPointer; i <= batchEndPointer; i += tuple_) {
//				long t = getTimestamp(i);
//				/* 
//				 * Should we open new windows? 
//				 */
//				boolean open = false;
//				while (t - slide_ >= this.batchStartTime + ((long) p) * windowDefinition.getSlide()) {
//					p ++;
//					open |= true;
//				}
//				if (open && p < this.batchSize)
//					this.windowStartPointers[p] = i;
//				/* 
//				 * Should we close old windows? 
//				 */
//				boolean close = true;
//				
//				while (t > this.batchStartTime + q * windowDefinition.getSlide() + windowDefinition.getSize() ) {
//					if (close)
//						this.windowEndPointers[q] = i;
//					close = false;
//					q ++;
//				}
//			} /* End of batch */
		}
	}
	
	public void initPrevWindowPointers () {
		
		/* Find start and end pointer of the last window
		 * from the previous batch.
		 */
		if (batchStartPointer < 0 && batchEndPointer < 0)
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
			
			this.prevStartPointer = this.batchStartPointer - offset;
			// check whether we crossed the buffer boundary
			if (this.prevStartPointer < 0)
				this.prevStartPointer += buffer.capacity();
			
			this.prevEndPointer = this.prevStartPointer + bpw; 
			
		} else { /* Find last range-based window */
			
			long previousStartTime = this.batchStartTime - windowDefinition.getSlide();
			/* We work our way backwards until we find the first tuple
			 * whose timestamp is less than `previousStartTime` 
			 */
			idx = batchStartPointer;
			long t = getTimestamp(idx);
			while (t >= previousStartTime) {
				idx -= tuple_;
				if (idx < 0)
					idx += buffer.capacity();
				t = getTimestamp(idx);
			}
			this.prevStartPointer = idx + tuple_;
			
			long previousEndTime = previousStartTime + windowDefinition.getSize();
			/* We work our way forward until we find the first tuple
			 * whose timestamp is greater than `previousEndTime` 
			 */
			idx = prevStartPointer;
			t = getTimestamp(idx);
			while (t < previousEndTime) {
				idx += tuple_;
				t = getTimestamp(idx);
			}
			this.prevEndPointer = idx;
		}
		
		System.out.println(String.format("[DBG] last window of previous batch starts %10d ends %10d",
				this.prevStartPointer, this.prevEndPointer));
	}
	
	public void moveFreePointerToNotFreeLastWindow () {
		/*
		 * Set new free pointer to the byte before the start of the last 
		 * window of this window batch
		 */
		this.freeOffset = this.windowStartPointers[this.windowStartPointers.length - 1] - 1;
		// check whether we need to wrap
		this.freeOffset = (this.freeOffset < 0) ? this.freeOffset + buffer.capacity() : this.freeOffset;
	}
	
	public void clear () {
		initialised = false;
		windowStartPointers = windowEndPointers = null;
		batchStartTime = batchEndTime = -1;
		this.buffer = null;
		this.prevStartPointer = this.prevEndPointer = -1;
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
	
	public int getBatchStartPointer () {
		return this.batchStartPointer;
	}
	
	public int getBatchEndPointer () {
		return this.batchEndPointer;
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
		
		for (int i = 0; i < this.batchSize; i++) {
			
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
		for (int i = 0; i < batchSize; i++) {
			
			windowStartPointers[i] -= batchStartPointer;
			windowEndPointers  [i] -= batchStartPointer;
		}
	}
	
	public void normalizeWindowPointers (int [] startPtrs, int [] endPtrs) {
		for (int i = 0; i < batchSize; i++) {
			startPtrs[i] -= batchStartPointer;
			endPtrs  [i] -= batchStartPointer;
		}
	}

	public int getPrevStartPointer() {
		return prevStartPointer;
	}

	public int getPrevEndPointer() {
		return prevEndPointer;
	}

	public int getLatencyMark() {
		return this.latencyMark;
	}
	
	public void setLatencyMark(int latencyMark) {
		this.latencyMark  = latencyMark;
	}
	
	/*
	 * Normalise a pointer to a location in the 
	 * underlying byte buffer.
	 * 
	 * Avoids "out of bounds" memory accesses,
	 * especially when we copy memory via the
	 * Unsafe interface.
	 */
	public int normalise(int pointer) {
		
		return this.buffer.normalise((long) pointer);
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
}

