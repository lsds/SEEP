package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class UnboundedQueryBuffer implements IQueryBuffer {
	
	ByteBuffer buffer;
	
	public UnboundedQueryBuffer (int size) {
		if (size <= 0)
			throw new IllegalArgumentException("Buffer size must be greater than 0."); 
		buffer = ByteBuffer.allocate(size); 
	}
	
	@Override
	public int getInt (int offset) { 
		return buffer.getInt(offset); 
	}
	
	@Override
	public float getFloat (int offset) { 
		return buffer.getFloat(offset); 
	}
	
	@Override
	public long getLong (int offset) { 
		return buffer.getLong(offset); 
	}
	
	@Override
	public byte [] array () { 
		return buffer.array(); 
	}
	
	@Override
	public ByteBuffer getByteBuffer () { 
		return buffer; 
	}
	
	@Override
	public int capacity () { 
		return buffer.capacity(); 
	}
	
	@Override
	public int remaining () { 
		return buffer.remaining(); 
	}
	
	@Override
	public boolean hasRemaining () { 
		return buffer.hasRemaining(); 
	}
	
	@Override
	public int position() {
		return buffer.position();
	}
	
	@Override
	public int limit () { 
		return buffer.limit(); 
	}
	
	@Override
	public void close () { 
		buffer.flip(); 
	}
	
	@Override
	public void clear () {
		buffer.clear();
	}
	
	/* The buffer is every growing based on peek demand */
	
	@Override
	@SuppressWarnings("finally")
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
	
	@Override
	public int putInt (int index, int value) {
		buffer.putInt(index, value);
		return 0;
	}
	
	@Override
	@SuppressWarnings("finally")
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
	
	@Override
	public int putFloat(int index, float value) {
		buffer.putFloat(index, value);
		return 0;
	}
	
	@Override
	@SuppressWarnings("finally")
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
	
	@Override
	public int putLong(int index, long value) {
		buffer.putLong(index, value);
		return 0;
	}
	
	@Override
	@SuppressWarnings("finally")
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
	
	@Override
	public int put(byte [] source, int offset, int length) {
		
		/* Check bounds and normalise indices of source byte array */
		
		buffer.put(source, offset, length);
		return 0;
	}
	
	@Override
	public int put (IQueryBuffer buffer) {
		return buffer.put(buffer.array());
	}
	
	@Override
	public int put (IQueryBuffer source, int offset, int length) {
		return put (source.array(), offset, length);
	}
	
	@Override
	public void resize () {
		int size = buffer.capacity();
		resize (size + size);
	}
	
	@Override
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
	
	@Override
	public void free (int index) {
		throw new UnsupportedOperationException
		("error: cannot free bytes in an unbounded buffer");
	}

	@Override
	public void release() {
		UnboundedQueryBufferFactory.free(this);
	}

	@Override
	public byte [] array (int offset, int length) {
		byte [] result = new byte [length];
		System.arraycopy(buffer.array(), offset, result, 0, length);
		return result;
	}

	@Override
	public void appendBytesTo (int offset, int length, IQueryBuffer toBuffer) {
		
		/* Check bounds and normalise indices of this byte array */
		
		toBuffer.put(this.buffer.array(), offset, length);
	}

	@Override
	public void appendBytesTo(int start, int end, byte[] destination) {
		
		System.arraycopy(this.buffer.array(), start, destination, 0, end - start);
	}
}
