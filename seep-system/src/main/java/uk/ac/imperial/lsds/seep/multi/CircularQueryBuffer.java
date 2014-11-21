package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

import java.lang.UnsupportedOperationException;
import java.util.concurrent.atomic.AtomicInteger;

public class CircularQueryBuffer implements IQueryBuffer {
	
	private static final int _default_capacity = 1048576;
	private byte [] data;
	private int size;
	private final AtomicInteger start;
	private final AtomicInteger end;
	int wraps;
	ByteBuffer buffer;
	
	private int asPowerOf2 (int size) {
		return 1 << (32 - Integer.numberOfLeadingZeros(size - 1));
	}
	
	public CircularQueryBuffer () {
		this (_default_capacity);
	}
	public CircularQueryBuffer (int _size) { 
		this.size = asPowerOf2 (_size); /* Align size to the next power of 2 */
		if (this.size <= 0)
			throw new IllegalArgumentException("Buffer size must be greater than 0.");
		System.out.println(String.format("[DBG] %d bytes", size));
		/* Also, check if buffer size is a multiple of tuple size */
		data = new byte [this.size];
		start = new AtomicInteger(0);
		end = new AtomicInteger(0);
		wraps = 0;
		buffer = ByteBuffer.wrap(data);
	}
	
	public int getInt (int offset) {
		return buffer.getInt(offset);
	}
	
	public float getFloat (int offset) {
		return buffer.getFloat(offset);
	}
	
	public long getLong (int offset) {
		return buffer.getLong(offset);
	}
	
	public byte [] array () {
		return data;
	}
	
	public ByteBuffer getByteBuffer () {
		return buffer;
	}
	
	public int capacity () {
		return size;
	}
	
	public int remaining () {
		int start_ = start.get();
		int end_ = end.get();
		if (start_ < end_)
			return (end_ - start_); 
		else
			return ((size - start_) + end_);
	}
	
	public boolean hasRemaining () {
		return (remaining() > 0);
	}
	
	public int limit () {
		return size;
	}
	
	public void close () {
		return ;
	}
	
	public void clear () {
		return ;
	}
	
	public int putInt (int value) {
		/* buffer.putInt(end, value); */
		throw new UnsupportedOperationException
		("error: cannot put int to a circular buffer");
	}
	
	public int putFloat (float value) {
		/* buffer.putFloat(end, value); */
		throw new UnsupportedOperationException
		("error: cannot put float to a circular buffer");
	}
	
	public int putLong (long value) {
		/* buffer.putLong(end, value); */
		throw new UnsupportedOperationException
		("error: cannot put long to a circular buffer");
	}
	
	public int put (byte [] values) {
		
		if (values.length > remaining()) {
			System.out.println("[DBG] 1");
			return -1;
		}
		
		int end_ = end.get();
		if (start.get() <= end_ - size) {
			System.out.println("[DBG] 2");
			return -1;
		}
		
		if (values.length > (size - end_)) { /* Copy in two parts */
			int right = size - end_;
			int left  = values.length - (size - end_);
			System.arraycopy(values, 0, data, end_, right);
			System.arraycopy(values, size - end_, data, 0, left);
			end.lazySet(left);
		} else {
			System.arraycopy(values, 0, data, end_, values.length);
			end.lazySet(normalise(end_ + values.length));
		}
		buffer.position(end.get());
		return end_;
	}
	
	public int put (IQueryBuffer buffer) {
		return put (buffer.array());
	}
	
	public void resize () {
		resize (this.size + this.size);
	}
	
	public void resize (int size_) {
		throw new UnsupportedOperationException
		("error: cannot resize a circular buffer");
	}
	
	public void free (int index) {
		int _start = normalise (index + 1);
		start.lazySet(_start);
	}
	
	private int normalise (int index) {
		return index & (size - 1); /* Iff. size is a power of 2 */
	}

	@Override
	public void release() { }

	@Override
	public byte[] getBytes(int offset, int length) {
		byte [] result = new byte [length];
		System.arraycopy(data, offset, result, 0, length);
		return result;
	}
}
