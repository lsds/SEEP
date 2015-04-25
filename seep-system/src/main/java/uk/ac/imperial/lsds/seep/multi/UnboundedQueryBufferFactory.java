package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class UnboundedQueryBufferFactory {
	
	private static final int _pool_size = 0; /* Initial pool size */
	private static final int _buffer_size = Utils._UNBOUNDED_BUFFER_;
	
	public static AtomicLong count = new AtomicLong(0L);
	
	private static ConcurrentLinkedQueue<IQueryBuffer> pool = 
		new ConcurrentLinkedQueue<IQueryBuffer>();
	
	static {
		int i = _pool_size;
		while (i-- > 0) {
			int id = (int) count.getAndIncrement();
			pool.add (new UnboundedQueryBuffer(id, _buffer_size, false));
		}
	}
	
	public static IQueryBuffer newInstance () {
		IQueryBuffer buffer;
		buffer = pool.poll();
		if (buffer == null) {
			int id = (int) count.getAndIncrement();
			return new UnboundedQueryBuffer(id, _buffer_size, false);
		}
		
		return buffer;
	}
	
	public static void free (IQueryBuffer buffer) {
		buffer.clear();
		/* The pool is ever growing based on peek demand */
		pool.offer (buffer);
	}
}
