import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import uk.ac.imperial.lsds.seep.multi.CircularQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TaskDispatcher;
import uk.ac.imperial.lsds.seep.multi.TaskExecutor;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class TestCircularQueryBuffer {
	
	private static int _threads = Utils.THREADS;
	private static int _queue_size = Utils.TASKS;
	
	public static void main (String [] args) {
		
		/* Timed measurements */
		long  bytes_consumed = 0L;
		long _bytes_consumed = 0L;
		long  bytes_produced = 0L;
		long _bytes_produced = 0L;
		long t, _t = 0L;
		double dt;
		double MB1, MB2, _1MB_ = 1048576.0;
		double MB1ps, MB2ps; /* MB/sec */
		
		BlockingQueue<Runnable> queue = 
			new ArrayBlockingQueue<Runnable>(_queue_size, true);
		
		ExecutorService executor = new TaskExecutor (_threads, _threads, 0L, TimeUnit.MILLISECONDS, queue);
		
		CircularQueryBuffer buffer = new CircularQueryBuffer();
		
		WindowDefinition windowDefinition = new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		
		TaskDispatcher dispatcher = new TaskDispatcher (executor, buffer, windowDefinition, schema);
		
		Producer producer = new Producer(dispatcher);
		Thread p = new Thread(producer);
		p.setName("Producer");
        p.start();
		
		try {
			while (true) {
				Thread.sleep(1000L);
				bytes_consumed = buffer.getBytesProcessed();
				bytes_produced = producer.getBytesSent();
				t = System.currentTimeMillis();
				if (_t > 0) {
					dt = ((double) (t - _t)) / 1000.;
					MB1 = ((double) (bytes_consumed - _bytes_consumed)) / _1MB_;
					MB1ps = MB1 / dt;
					MB2 = ((double) (bytes_produced - _bytes_produced)) / _1MB_;
					MB2ps = MB2 / dt;
					System.out.println(
					String.format("[DBG] %10.3f MB/s %10.3f Gbps %13d bytes consumed %13d bytes produced [delta %13d] %10.3f MB/s (%d tasks queued)",
					MB1ps, 
					((MB1ps / 1024.) * 8.), 
					bytes_consumed, 
					bytes_produced, 
					(bytes_produced - _bytes_produced) - (bytes_consumed - _bytes_consumed),
					MB2ps,
					queue.size()));
				}
				_t = t;
				_bytes_consumed = bytes_consumed;
				_bytes_produced = bytes_produced;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit (1);
		}
	}
	
	public static class Producer implements Runnable {
		
		private TaskDispatcher dispatcher;
		private AtomicLong bytesSent = new AtomicLong(0L);
		
		public Producer (TaskDispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}
		
		public long getBytesSent () {
			return bytesSent.get();
		}
		
		public void run () {
			/* Initialise input data */
			byte [] data = new byte [Utils.BUNDLE];
			ByteBuffer b = ByteBuffer.wrap(data);
			while (b.hasRemaining())
				b.putInt(1);
			try {
				while (true) {
					dispatcher.dispatch (data);
					bytesSent.getAndAdd(data.length);
				}
			} catch (Exception e) { 
				e.printStackTrace(); 
				System.exit(1);
			}
		}
	}
}

