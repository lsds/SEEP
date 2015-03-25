package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

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
			// pool.add (new UnsafeUnboundedQueryBuffer(id, _buffer_size));
		}
	}
	
	public static IQueryBuffer newInstance () {
		IQueryBuffer buffer;
		
//		if (count++ < _pool_size) {
//			buffer = new UnboundedQueryBuffer(_buffer_size);
//		} else {
//		
//			while ((buffer = pool.poll()) == null) {
////				 System.err.println(String.format("warning: thread %20s blocked waiting for a buffer to become available in the pool", 
////						Thread.currentThread()));
//				// LockSupport.parkNanos(1L);
//				LockSupport.parkNanos(1L);
//			}
//		}
		buffer = pool.poll();
		if (buffer == null) {
			int id = (int) count.getAndIncrement();
			return new UnboundedQueryBuffer(id, _buffer_size, false);
			// return new UnsafeUnboundedQueryBuffer(id, _buffer_size);
		}
		
		return buffer;
	}
	
	public static void free (IQueryBuffer buffer) {
		buffer.clear();
		
		// System.out.println("FREE BUFFER " + buffer + " position " + buffer.position());

//		if (buffer.position() != 0) {
//			System.err.println("error: IN BUFFER buffer not reset (" + buffer.position() + ")");
//			System.exit(1);
//		}
//		
//		if (pool.contains(buffer)) {
//			System.err.println("error: buffer already in pool");
//			System.exit(1);
//		}
		
		/* The pool is ever growing based on peek demand */
		pool.offer (buffer);
	}
}
