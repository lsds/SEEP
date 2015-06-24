package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PartialWindowResults {
	
	int pid; /* The worker that requested this object. */
	
	IQueryBuffer buffer; /* The buffer that holds the partial window results */
	
	boolean empty;
	
	int size;
	int count;
	
	int [] startPointers;
	
	private static final int max_windows = 32768; 
	
	public PartialWindowResults (int pid) {
		
		this.pid = pid;
		
		this.buffer = null;
		this.size = 0;
		this.count = 0;
		
		startPointers = new int [max_windows];
		Arrays.fill(startPointers, -1);
	}
	
	public void setBuffer(IQueryBuffer buffer) {
		this.buffer = buffer;
		this.size = buffer.position();
	}
	
	public IQueryBuffer getBuffer () {
		return buffer;
	}
	
	public boolean isEmpty () {
		if (buffer == null)
			return true;
		else
			return (this.buffer.position() == 0);
	}
	
	public void release () {
		
		if (buffer != null) {
			buffer.release();
		}
		buffer = null;
		size = 0;
		for (int i = 0; i < count; i++)
			startPointers[i] = -1;
		count = 0;
		PartialWindowResultsFactory.free(this.pid, this);
	}
	
	public void init() {
		
		this.buffer = null;
		this.size = 0;
		this.count = 0;
	}
	
	public void nullify () {
		
		this.buffer.clear();
		this.size = 0;
		this.count = 0;
	}
	
	public void increment() {
		if (count >= max_windows)
			throw new IndexOutOfBoundsException ("error: operator exceeded maximum number of partial window results");
		
		startPointers[count++] = buffer.position();
	}
	
	public int numberOfWindows() {
		
		return count;
	}

	public void shiftLeft(int N, int outputTupleSize) {
		
		int startOffset = N * outputTupleSize;
		
		if (N + this.count >= max_windows)
			throw new IndexOutOfBoundsException ("error: operator exceeded maximum number of partial window results");
		
		for (int i = count - 1; i >= 0; i--)
			startPointers[i + N] = startPointers[i] + startOffset;
		
		startPointers[0] = 0;
		for (int i = 1; i < N; i++)
			startPointers[i] = startPointers[i - 1] + outputTupleSize;
		
		this.count += N;
	}
	
	public void shiftLeft(int N, int offset, int [] otherPointers) {
		
		if (N + this.count >= max_windows)
			throw new IndexOutOfBoundsException ("error: operator exceeded maximum number of partial window results");
		
		for (int i = count - 1; i >= 0; i--)
			startPointers[i + N] = startPointers[i] + offset;
		
		startPointers[0] = 0;
		for (int i = 1; i < N; i++) {
			startPointers[i] = otherPointers[i + 1] - otherPointers[1];
		}
		
		this.count += N;
	}
	
	public void prepend (ByteBuffer d, int offset, int length, ByteBuffer t) {
		ByteBuffer b = this.buffer.getByteBuffer();
		t.clear();
		t.put(b.array(), 0, b.position());
		b.clear();
		b.put(d.array(), offset, length);
		t.flip();
		b.put(t);
	}
}
