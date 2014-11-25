package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class UnboundedQueryBuffer implements IQueryBuffer {
	
	ByteBuffer buffer;
	
	public UnboundedQueryBuffer (int size) {
		if (size <= 0)
			throw new IllegalArgumentException("Buffer size must be greater than 0."); 
		buffer = ByteBuffer.allocate(size); 
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
		return buffer.array(); 
	}
	
	public ByteBuffer getByteBuffer () { 
		return buffer; 
	}
	
	public int capacity () { 
		return buffer.capacity(); 
	}
	
	public int remaining () { 
		return buffer.remaining(); 
	}
	
	public boolean hasRemaining () { 
		return buffer.hasRemaining(); 
	}
	
	public int limit () { 
		return buffer.limit(); 
	}
	
	public void close () { 
		buffer.flip(); 
	}
	
	public void clear () {
		buffer.clear();
	}
	
	/* The buffer is every growing based on peek demand */
	
	public int putInt (int value) { 
		try {
			buffer.putInt(value);
		} catch (IndexOutOfBoundsException e) {
			resize ();
			putInt (value);
		} finally {
			return 0;
		}
	}
	
	public int putFloat (float value) {
		try {
			buffer.putFloat(value);
		} catch (IndexOutOfBoundsException e) {
			resize ();
			putFloat (value);
		} finally {
			return 0;
		}
	}
	
	public int putLong (long value) {
		try {
			buffer.putLong(value);
		} catch (IndexOutOfBoundsException e) {
			resize ();
			putLong (value);
		} finally {
			return 0;
		}
	}
	
	public int put (byte [] values) {
		int size, size_;
		try {
			buffer.put(values);
		} catch (IndexOutOfBoundsException e) {
			size  = buffer.capacity();
			size_ = (values.length < size) ? (size + size) : (size + values.length);
			resize (size_);
			put (values);
		} finally {
			return 0;
		}
	}
	
	public int put (IQueryBuffer buffer) {
		return buffer.put(buffer.array());
	}
	
	public void resize () {
		int size = buffer.capacity();
		resize (size + size);
	}
	
	public void resize (int size) {
		if (size <= buffer.capacity())
			return ;
		int offset = buffer.position();
		buffer.flip();
		ByteBuffer buffer_ = ByteBuffer.allocate(size);
		buffer_.put(buffer);
		buffer = buffer_;
		buffer.position(offset);
	}
	
	public void free (int index) {
		throw new UnsupportedOperationException
		("error: cannot free bytes in an unbounded buffer");
	}

	@Override
	public void release() {
		UnboundedQueryBufferFactory.free(this);
	}

	@Override
	public byte[] array(int offset, int length) {
		byte [] result = new byte [length];
		System.arraycopy(buffer.array(), offset, result, 0, length);
		return result;
	}

	@Override
	public void copyBytesTo(int offset, int length, IQueryBuffer toBuffer) {
		toBuffer.getByteBuffer().put(this.buffer.array(), offset, length);
	}
}
