import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

public class SmartGridWorker implements Runnable {
	
	/*
	 * The byte array (`buf`) where the data is stored for the output stream
	 * is by default `protected` and the base class always returns a copy.
	 *
	 * We extend the class to return `buf` directly.
	 * 
	 */
	private class ___ByteArrayOutputStream extends ByteArrayOutputStream {
		
		public ___ByteArrayOutputStream (int size) { super(size); }
		public byte [] getBuffer () { return buf; }
	}
	
	@SuppressWarnings("unused")
	private class ___ByteArrayInputStream extends ByteArrayInputStream {
		
		public ___ByteArrayInputStream (byte [] buffer) { super(buffer); }
		public byte [] getBuffer () { return buf; }
		
		public void writeTo (byte [] buffer) {
			
			if (this.buf.length < buffer.length) {
				throw new IndexOutOfBoundsException();
			}
			
			System.arraycopy(buffer, 0, this.buf, 0, buffer.length);
			this.pos = 0;
			this.count = buffer.length;
		}
	}
	
	private ConcurrentLinkedQueue<ByteBuffer> pool; /* The buffer pool  */
	private static final int _pool_size = 10; /* The buffer pool's size */
	
	/* List of compressed bundles, shared by all workers */
	private ArrayList<ByteBuffer> list;
	
	private int idx; /* Thread id */
	private int max;

	private int _bundle; /* The uncompressed bundle size */
	
	/* Queue of decompressed bundles */
	private ConcurrentLinkedQueue<ByteBuffer> queue;
	
	private boolean finished = false;
	
	private ___ByteArrayOutputStream output;
	private ___ByteArrayInputStream   input;
	
	private byte [] buffer;
	
	public SmartGridWorker (ArrayList<ByteBuffer> list, int idx, int max, int _bundle) {
		
		this.list = list;
		
		this.idx = idx;
		this.max = max;
		
		this._bundle = _bundle;
		
		pool = new ConcurrentLinkedQueue<ByteBuffer>();
		int i = _pool_size;
		while (i-- > 0) {
			pool.add (ByteBuffer.allocate(_bundle));
		}
		
		this.output = new ___ByteArrayOutputStream(this._bundle);
		this.input  = new ___ByteArrayInputStream (new byte [this._bundle]);
		
		this.queue = new ConcurrentLinkedQueue<ByteBuffer>();
		this.buffer = new byte [_bundle];
		
		System.out.println(String.format("[DBG] thread %d/%d starts", 
			idx, max));
	}
	
	@Override
	public void run() {
		
		try {
			int next = idx;
			int wraps = 0;
			while (next < list.size() && wraps < 100) {
				
				decompress(next);
				next += max;
				/*
				 * Reset (thread enters an infinite loop)
				 */
				
//				if (next >= list.size()) {
//					next = idx;
//					wraps += 1;
//				}
				
			}
		
		} catch (Exception e) {
			
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(String.format("[DBG] thread %d/%d exits", 
			idx, max));
		this.finished = true;
	}
	
	private void decompress (int next) 
		throws IOException, InterruptedException {
		
		ByteBuffer b, d;
		/* Pick next element on list */
		b = list.get(next);
		/* Do not exceed upper bound on elements in queue */
		while ((d = pool.poll()) == null)
			Thread.yield();
		decompress (b, d);
		/* Prepare data for reading */
		d.flip();
		queue.offer(d);
	}
	
	public ByteBuffer poll () throws InterruptedException {
		
		ByteBuffer b;
		while ((b = queue.poll()) == null && ! finished)
			Thread.yield();
		return b;
	}
	
	public void free (ByteBuffer b) {
		/* 
		 * The main has just pushed the buffer over
		 * the network and returns it to the pool 
		 */
		b.clear();
		pool.offer(b);
	}
	
	private void decompress (ByteBuffer b, ByteBuffer d) 
		throws IOException {
		
		/* Reset ByteArrayOutputStream */
		output.reset();
		if (output.getBuffer().length != this._bundle) {
			System.out.println(String.format("warning (%d): output stream count %6d size %6d", 
					idx, output.size(), output.getBuffer().length));
		}
		
		/* ByteArrayInputStream input = new ByteArrayInputStream (b.array()); */
		input.writeTo(b.array());
		GZIPInputStream gzip = new GZIPInputStream (input);
		
		int n;
		while ((n = gzip.read (buffer)) >= 0)
			output.write (buffer, 0, n);
		gzip.close ();
		/* Copy the stream's buffer to the the byte buffer */
		d.put(output.getBuffer(), 0, output.size());
    }
}

