package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

public class UnboundedQueryBufferFactory {
	
	private static final int   _pool_size = 10; /* Initial pool size */
	private static final int _buffer_size = 1048576; /* 1MB */
	
	private static ConcurrentLinkedQueue<UnboundedQueryBuffer> pool = 
		new ConcurrentLinkedQueue<UnboundedQueryBuffer>();
	
	static {
		int i = _pool_size;
		while (i-- > 0)
			pool.add (new UnboundedQueryBuffer(_buffer_size));
	}
	
	public static UnboundedQueryBuffer newInstance () {
		UnboundedQueryBuffer buffer = pool.poll();
		if (buffer == null)
			return new UnboundedQueryBuffer(_buffer_size);
		return buffer;
	}
	
	public static void free (UnboundedQueryBuffer buffer) {
		buffer.clear();
		/* The pool is ever growing based on peek demand */
		pool.offer (buffer);
	}
}
