package uk.ac.imperial.lsds.seep.multi;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;


public class CircularQueryBuffer implements IQueryBuffer {
	
	private static final int _default_capacity = Utils._CIRCULAR_BUFFER_;
	private byte [] data;
	private int size;
	private final PaddedAtomicLong start;
	private final PaddedAtomicLong end;
	private long mask;
	long wraps;
	ByteBuffer buffer;
	
	private AtomicLong bytesProcessed;
	private PaddedLong h;
	
	private static int nextPowerOfTwo (int size) {
		return 1 << (32 - Integer.numberOfLeadingZeros(size - 1));
	}
	
	public CircularQueryBuffer () {
		this (_default_capacity);
	}
	
	public CircularQueryBuffer (int _size) { 
		
		if (_size <= 0)
			throw new IllegalArgumentException("error: buffer size must be greater than 0");
		
		this.size = nextPowerOfTwo (_size); /* Set size to the next power of 2 */
		
		if (Integer.bitCount(this.size) != 1)
			throw new IllegalArgumentException("error: buffer size must be a power of 2");
		
		System.out.println(String.format("[DBG] %d bytes", size));
		
		/* Also, check if buffer size is a multiple of tuple size */
		
		data = new byte [this.size];
		start = new PaddedAtomicLong (0L);
		end = new PaddedAtomicLong (0L);
		mask = this.size - 1;
		wraps = 0;
		buffer = ByteBuffer.wrap(data);
		bytesProcessed = new AtomicLong (0L);
		h = new PaddedLong (0L);
	}
	
	@Override
	public int getInt (int offset) {
		return buffer.getInt(normalise(offset));
	}
	
	@Override
	public float getFloat (int offset) {
		return buffer.getFloat(normalise(offset));
	}
	
	@Override
	public long getLong (int offset) {
		return buffer.getLong(normalise(offset));
	}
	
	@Override
	public byte [] array () {
		return data;
	}
	
	/* Avoid using this function, as it creates intermediate byte [] objects */
	@Override
	public byte [] array (int offset, int length) {
		byte [] result = new byte [length];
		System.arraycopy(data, offset, result, 0, length);
		return result;
	}
	
	@Override
	public ByteBuffer getByteBuffer () {
		return buffer;
	}
	
	@Override
	public int capacity () {
		return size;
	}
	
	@Override
	public int remaining () {
		long tail = end.get();
		long head = start.get();
		if (tail < head)
			return (int) (head - tail);
		else
			return size - (int) (tail - head);
	}
	
	@Override
	public boolean hasRemaining () {
		return (remaining() > 0);
	}
	
	public boolean hasRemaining (int length) {
		return (remaining() >= length);
	}
	
	@Override
	public int limit () {
		return size;
	}
	
	@Override
	public void close () {
		return ;
	}
	
	@Override
	public void clear () {
		return ;
	}
	
	@Override
	public int putInt (int value) {
		throw new UnsupportedOperationException("error: cannot put int to a circular buffer");
	}
	
	@Override
	public int putInt (int index, int value) {
		throw new UnsupportedOperationException("error: cannot put int to a circular buffer");
	}
	
	@Override
	public int putFloat (float value) {
		throw new UnsupportedOperationException("error: cannot put float to a circular buffer");
	}
	
	@Override
	public int putFloat (int index, float value) {
		throw new UnsupportedOperationException("error: cannot put float to a circular buffer");
	}
	
	@Override
	public int putLong (long value) {
		throw new UnsupportedOperationException("error: cannot put long to a circular buffer");
	}
	
	@Override
	public int putLong (int index, long value) {
		throw new UnsupportedOperationException("error: cannot put long to a circular buffer");
	}
	
	@Override
	public int put (byte [] values) {
		
		if (values == null)
			throw new NullPointerException("error: cannot put null values to a circular buffer");
		
		final long _end = end.get();
		final long wrapPoint = (_end + values.length - 1) - size;
		if (h.value <= wrapPoint) {
			h.value = start.get();
			if (h.value <= wrapPoint) {
				/* debug (); */
				return -1;
			}
		}
		
		int index = normalise (_end);
		if (values.length > (size - index)) { /* Copy in two parts */
			int right = size - index;
			int left  = values.length - (size - index);
			System.arraycopy(values, 0, data, index, right);
			System.arraycopy(values, size - index, data, 0, left);
			//System.out.println(String.format("[DBG] part I [%d, %d) part II [0, %d)", index, right, left));
		} else {
			System.arraycopy(values, 0, data, index, values.length);
		}
		int p = normalise (_end + values.length);
		if (p <= index)
			wraps ++;
		/* buffer.position(p); */
		end.lazySet(_end + values.length);
		return index;
	}
	
	@Override
	public int put (byte[] source, int offset, int length) {
		return 0;
	}
	
	@Override
	public int put (IQueryBuffer buffer) {
		return put (buffer.array());
	}
	
	@Override
	public int put (IQueryBuffer source, int offset, int length) {
		return 0;
	}
	
	@Override
	public void resize () {
		resize (this.size + this.size);
	}
	
	@Override
	public void resize (int size_) {
		throw new UnsupportedOperationException("error: cannot resize a circular buffer");
	}
	
	@Override
	public void free (int offset) {
		final long _start = start.get();
		final int index = normalise (_start);
		final int bytes;
		/* Measurements */
		if (offset <= index)
			bytes = size - index + offset + 1;
		else
			bytes = offset - index + 1;
		/* System.out.println(String.format("[DBG] %6d bytes processed; new start is %6d", bytes, _start + bytes)); */
		bytesProcessed.addAndGet(bytes);
		/* Set new start pointer */
		start.lazySet(_start + bytes);
	}
	
	@Override
	public void release () {
		return ;
	}
	
	@Override
	public int normalise (long index) {
		return (int) (index & mask); /* Iff. size is a power of 2 */
	}
	
	public long getBytesProcessed () { 
		return bytesProcessed.get(); 
	}
	
	public long getWraps () {
		return wraps;
	}
	
	public void debug () {
		long head = start.get();
		long tail = end.get();
		int remaining = (tail < head) ? (int) (head - tail) : (size - (int) (tail - head));
		System.out.println(
		String.format("[DBG] start %7d [%7d] end %7d [%7d] %7d wraps %13d bytes remaining", 
		normalise(head), head, normalise(tail), tail, getWraps(), remaining));
	}

	@Override
	public void appendBytesTo (int offset, int length, IQueryBuffer destination) {
		int start = normalise(offset);
		destination.getByteBuffer().put(this.data, start, length);
	}
	
	@Override
	public void appendBytesTo (int start, int end, byte [] destination) {
		if (end > start) {
			System.arraycopy(this.data, start, destination, 0, end - start);
		} else {
			/* Copy in two parts */
			System.arraycopy(this.data, start, destination, 0, this.size - start);
			System.arraycopy(this.data, 0, destination, 0, end);
		}
	}
	
	@Override
	public int position () {
		throw new UnsupportedOperationException("error: cannot get position from a circular buffer");
	}

	@Override
	public void position(int index) {
		throw new UnsupportedOperationException("error: cannot set position to a circular buffer");
	}
}
