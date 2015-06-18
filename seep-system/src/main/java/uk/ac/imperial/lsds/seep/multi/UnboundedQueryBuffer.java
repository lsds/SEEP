package uk.ac.imperial.lsds.seep.multi;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class UnboundedQueryBuffer implements IQueryBuffer {
	
	ByteBuffer buffer;
	
	private boolean isDirect = false;
	
	private int id;
	
	public UnboundedQueryBuffer (int id, int size, boolean isDirect) {
		
		if (size <= 0)
			throw new IllegalArgumentException("Buffer size must be greater than 0.");
		
		this.id = id;
		this.isDirect = isDirect;
		
		if (! isDirect) {
			buffer = ByteBuffer.allocate(size);
		} else {
			buffer = ByteBuffer.allocateDirect(size);
		}
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
		if (! this.isDirect)
			return buffer.array();
		else
			throw new UnsupportedOperationException("error: cannot get array from a direct buffer");
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
	
	@Override
	@SuppressWarnings("finally")
	public int putInt (int value) { 
		try {
			buffer.putInt(value);
		} catch (BufferOverflowException e) {
			e.printStackTrace();
			/* resize ();
			putInt (value); */
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
		} catch (BufferOverflowException e) {
			e.printStackTrace();
			/* resize ();
			putFloat (value); */
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
		} catch (BufferOverflowException e) {
			e.printStackTrace();
			/* resize ();
			putLong (value); */
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
		/* int size, size_; */
		try {
			buffer.put(values);
		} catch (BufferOverflowException e) {
			e.printStackTrace();
			/* size  = buffer.capacity();
			size_ = (values.length < size) ? (size + size) : (size + values.length);
			resize (size_);
			put (values); */
		} finally {
			return 0;
		}
	}
	
	@Override
	public int put (byte [] source, int offset, int length) {
		/* Check bounds and normalise indices of source byte array */
		buffer.put(source, offset, length);
		return 0;
	}
	
	@Override
	public int put (byte [] values, int length) {
		buffer.put(values, 0, length);
		return 0;
	}
	
	@Override
	public int put (IQueryBuffer buffer) {
		return buffer.put(buffer);
	}
	
	@Override
	public int put (IQueryBuffer source, int offset, int length) {
		// System.out.println("put " + offset + " " + length + " remaining " + buffer.remaining());
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
		throw new UnsupportedOperationException("error: cannot free an unbounded buffer");
	}

	@Override
	public void release() {
		UnboundedQueryBufferFactory.free(this);
	}
	
	@Override
	public byte [] array (int offset, int length) {
		
		if (isDirect)
			throw new UnsupportedOperationException("error: cannot get array from a direct buffer");
		
		byte [] result = new byte [length];
		System.arraycopy(buffer.array(), offset, result, 0, length);
		return result;
	}

	@Override
	public void appendBytesTo (int offset, int length, IQueryBuffer destination) {
		
		/* Check bounds and normalise indices of this byte array */
		if (isDirect)
			throw new UnsupportedOperationException("error: cannot append bytes to a direct buffer");
		
		destination.put(this.buffer.array(), offset, length);
	}
	
	@Override
	public void appendBytesTo (int start, int end, byte [] destination) {
		
		if (isDirect)
			throw new UnsupportedOperationException("error: cannot append bytes to a direct buffer");
		
		System.arraycopy(this.buffer.array(), start, destination, 0, end - start);
	}
	
	@Override
	public void position(int index) {
		
		this.buffer.position(index);
	}

	@Override
	public int normalise(long index) {
		return (int) index;
	}
	
	@Override
	public long getBytesProcessed() {
		
		throw new UnsupportedOperationException("error: cannot get bytes processed from an unbounded buffer");
	}

	@Override
	public boolean isDirect() {
		return this.isDirect;
	}

	@Override
	public int getBufferId() {
		return id;
	}
}
