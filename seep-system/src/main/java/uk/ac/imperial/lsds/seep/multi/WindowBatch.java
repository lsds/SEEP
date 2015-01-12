package uk.ac.imperial.lsds.seep.multi;

import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class WindowBatch {
	
	private int batchSize;
	
	private IQueryBuffer buffer;
	private WindowDefinition windowDefinition;
	private ITupleSchema schema;
	
	private int batchStartPointer;
	private int batchEndPointer;
	
	private int [] windowStartPointers;
	private int [] windowEndPointers;
	
	private boolean initialised = false;
	
	/* Expected timestamps, based on range and slide of window definition */
	private long batchStartTime;
	private long batchEndTime;
	
	public WindowBatch () {
		this(0, null, null, null);
	}
	
	public WindowBatch (int batchSize, 
                        IQueryBuffer buffer, 
                        WindowDefinition windowDefinition, 
                        ITupleSchema schema) {
		
		this.batchSize = batchSize;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		this.windowStartPointers = null;
		this.windowEndPointers = null;
		
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
	}
	
	public void set (int batchSize, 
                     IQueryBuffer buffer, 
                     WindowDefinition windowDefinition, 
                     ITupleSchema schema) {
		
		this.batchSize = batchSize;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		
		this.batchStartPointer = -1;
		this.batchEndPointer = -1;
		this.windowStartPointers = null;
		this.windowEndPointers = null;
		
		this.initialised = false;
		
		this.batchStartTime = -1;
		this.batchEndTime = -1;
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
		this.batchStartTime = batchStartTime;
		this.batchEndTime = batchEndTime;
	}
	
	public void initWindowPointers (int [] windowStartPointers, int [] windowEndPointers) {
		
		if (initialised)
			return ;
		
		initialised = true;
		
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
			
			for (int i = batchStartPointer; i <= batchEndPointer; i += tuple_) {
				long t = buffer.getLong(i);
				/* 
				 * Should we open new windows? 
				 */
				boolean open = false;
				while (t - slide_ >= this.batchStartTime + p * slide_) {
					p ++;
					open |= true;
				}
				if (open && p < this.batchSize)
					this.windowStartPointers[p] = i;
				/* 
				 * Should be close old windows? 
				 */
				boolean close = true;
				while (t > this.batchStartTime + q * slide_ + window_ - 1) {
					if (close)
						this.windowEndPointers[q] = i;
					close = false;
					q ++;
				}
			} /* End of batch */
		}
	}
	
	public void initWindowPointers () {
		
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
			
			for (int i = batchStartPointer; i <= batchEndPointer; i += tuple_) {
				long t = buffer.getLong(i);
				/* 
				 * Should we open new windows? 
				 */
				boolean open = false;
				while (t - slide_ >= this.batchStartTime + p * slide_) {
					p ++;
					open |= true;
				}
				if (open && p < this.batchSize)
					this.windowStartPointers[p] = i;
				/* 
				 * Should be close old windows? 
				 */
				boolean close = true;
				while (t > this.batchStartTime + q * slide_ + window_ - 1) {
					if (close)
						this.windowEndPointers[q] = i;
					close = false;
					q ++;
				}
			} /* End of batch */
		}
	}
	
	public void clear () {
		initialised = false;
		windowStartPointers = windowEndPointers = null;
		batchStartTime = batchEndTime = -1;
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
			
			System.out.println(String.format("[DBG] window %3d starts at %10d ends at %10d %10d bytes %10d tuples", i, start, end, bytes, tuples));
		}
	}

	public void setWindowStartPointers(int[] windowStartPointers) {
		this.windowStartPointers = windowStartPointers;
	}

	public void setWindowEndPointers(int[] windowEndPointers) {
		this.windowEndPointers = windowEndPointers;
	}

	public void normalizeWindowPointers() {
		for (int i = 0; i < batchSize; i++) {
			windowStartPointers[i] -= batchStartPointer;
			windowEndPointers  [i] -= batchStartPointer;
		}
	}
}

