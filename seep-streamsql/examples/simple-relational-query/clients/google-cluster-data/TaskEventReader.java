import java.net.InetSocketAddress;
import java.net.InetAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskEventReader {
	
	private static final String usage = "usage: java TaskEventReader [-h host] [-p port] [-b tuples/bundle]";
	
	public static void main (String [] args) {
		
		String hostname = "localhost";
		int port = 6667;
		
		int tupleSize = 64;
		int bundle = 512;
		
		String filename = "compressed-512.dat";
		
		FileInputStream f;
		
		/* Time measurements */
		long start = 0L;
		long bytes = 0L;
		double dt;
		double _1MB = 1024. * 1024.;
		double MBps; /* MB/sec */
		
		long percent_ = 0L, _percent = 0L;
		
		int NTHREADS = 1;
		
		/* Parse command line arguments */
		int i, j;
		for (i = 0; i < args.length; ) {
			if ((j = i + 1) == args.length) {
				System.err.println(usage);
				System.exit(1);
			}
			if (args[i].equals("-f")) { 
				filename = args[j];
			} else
			if (args[i].equals("-h")) { 
				hostname = args[j];
			} else
			if (args[i].equals("-p")) { 
				port = Integer.parseInt(args[j]);
			} else
			if (args[i].equals("-b")) { 
				bundle = Integer.parseInt(args[j]);
			} else {
				System.err.println(String.format("error: unknown flag %s %s", args[i], args[j]));
				System.exit(1);
			}
			i = j + 1;
		}
		
		ArrayList<ByteBuffer> bundles = new ArrayList<ByteBuffer>();
		
		try {
			
			/* Establish connection to the server */
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(true);
			InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(hostname), port);
			System.out.println(address);
			channel.connect(address);
			
			while (! channel.finishConnect())
				;
			
			start = System.currentTimeMillis();
			
			/* Load file into memory */
			File file = new File (filename);
			long length = file.length();
			f = new FileInputStream(file);
			byte [] L = new byte [4];
			bytes = 0L;
			while (bytes < length) {
				f.read (L, 0, 4);
				int _bundle_ = ByteBuffer.wrap(L).getInt();
				byte [] compressed = new byte [_bundle_];
				f.read (compressed, 0, _bundle_);
				ByteBuffer buffer = ByteBuffer.wrap(compressed);
				bundles.add(buffer);
				bytes += 4; /* length */
				bytes += _bundle_; /* bundle */
				
				percent_ = (bytes * 100) / length;
				if (percent_ == (_percent + 1)) {
					System.out.print(String.format("Loading compressed file...%3d%%\r", percent_));
					_percent = percent_;
				}
			}
			f.close();
			
			dt = (double ) (System.currentTimeMillis() - start) / 1000.;
			/* Statistics */
			MBps = ((double) bytes / _1MB) / dt;
			
			System.out.println();
			System.out.println(String.format("[DBG] %12d bytes read", bytes));
			System.out.println(String.format("[DBG] %12d bundles", bundles.size()));
			System.out.println();
			System.out.println(String.format("[DBG] %10.1f seconds", (double) dt));
			System.out.println(String.format("[DBG] %10.1f MB/s", MBps));
			System.out.println();
			
			/* Send data */
			bytes = 0L;
			
			int _bundle = tupleSize * bundle;
			/* Launch worker threads */
			TaskEventWorker [] workers = new TaskEventWorker [NTHREADS];
			ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
			
			int iterations = 10;
			
			for (int idx = 0; idx < workers.length; idx++) {
				workers[idx] = new TaskEventWorker (bundles, idx, workers.length, _bundle, iterations, 2505600);
				executor.execute(workers[idx]);
			}
			
			int tid = 0; /* Poll all threads in round-robin fashion */
			ByteBuffer buffer;
			
			long _count = 0;
			for (int k = 0; k < iterations * bundles.size(); k++) {
				
				buffer = workers[tid].poll ();
				bytes += channel.write(buffer);
				/* Return the buffer */
				workers[tid].free(buffer);
				tid += 1;
				tid %= NTHREADS;
				
				_count ++;
			}
			
			executor.shutdown();
			
			System.out.println(String.format("[DBG] %12d bundles", _count));
			System.out.println(String.format("[DBG] %12d bytes sent", bytes));
			System.out.println("Bye.");
			
		} catch (Exception e) {
			System.err.println(String.format("error: %s", e.getMessage()));
			/* e.printStackTrace(); */
			System.exit(1);
		}
	}
}

