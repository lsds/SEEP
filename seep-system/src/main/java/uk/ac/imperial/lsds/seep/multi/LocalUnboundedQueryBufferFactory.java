package uk.ac.imperial.lsds.seep.multi;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class LocalUnboundedQueryBufferFactory {
	
	private final int _buffer_size = Utils._UNBOUNDED_BUFFER_;
	
	//public AtomicLong count;
	
	private Queue<UnboundedQueryBuffer> pool = 
		new LinkedList<UnboundedQueryBuffer>();
	
	public UnboundedQueryBuffer newInstance () {
		UnboundedQueryBuffer buffer;
		
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
			//count.incrementAndGet();
			return new UnboundedQueryBuffer(_buffer_size, this);
		}
		
		return buffer;
	}
	
	public void free (UnboundedQueryBuffer buffer) {
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
