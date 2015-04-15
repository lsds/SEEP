import java.net.InetSocketAddress;
import java.net.InetAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.io.File;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskEventClient {
	
	private static final String usage = "usage: java TaskEventClient";
	
	public static void main (String [] args) {
		
		String hostname = "localhost [-h host] [-p port] [-b tuples/bundle]";
		int port = 6667;
		
		int tupleSize = 64;
		int bundle = 512;
		
		String filename;
		int _PARTS = 500;
		
		FileInputStream f;
		DataInputStream d;
		BufferedReader  b;
		
		String line = null;
		long lines = 0;
		
		/* Time measurements */
		long start = 0L;
		long bytes = 0L;
		double dt;
		double rate; /* tuples/sec */
		double _1MB = 1024. * 1024.;
		double MBps; /* MB/sec */
		long totalTuples = 0;
		
		/* Count degenerate cases */
		long wrongTuples = 0L;
		
		long missing = 0L;
		long invalid = 0L;
		
		int NTHREADS = 7;
		
		/* Parse command line arguments */
		int i, j;
		for (i = 0; i < args.length; ) {
			if ((j = i + 1) == args.length) {
				System.err.println(usage);
				System.exit(1);
			}
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
		
		ByteBuffer data = ByteBuffer.allocate(tupleSize * bundle);
		byte [] compressed;
		ArrayList<ByteBuffer> bundles = new ArrayList<ByteBuffer>();
		
		TaskEventTuple tuple = new TaskEventTuple ();
		
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
			long tuple_counter = 0L;
			long compressedBytes = 0L;
			
			for (int fc = 0; fc < _PARTS; ++fc) { /* Task event file counter */
				
				filename = String.format("data/part-%05d-of-00500.csv", fc);
				
				File file = new File (filename);
				if (! file.exists()) {
					System.err.println(String.format("warning: file %s does not exist", filename));
					continue;
				}
				
				System.out.print(String.format("Loading task events [part %3d/%3d]\r", 
					(fc + 1), _PARTS));
				
				/* Load file into memory */
				f = new FileInputStream(filename);
				d = new DataInputStream(f);
				b = new BufferedReader(new InputStreamReader(d));
			
				while ((line = b.readLine()) != null) {
					lines += 1;
					bytes += line.length() + 1; // +1 for '\n'
				
					TaskEventTuple.parse(line, tuple);
					
					/* Consistency check */
					if (tuple.getMissing  () > 0) missing ++;
					if (tuple.getMachineId() < 1) invalid ++;
					if (tuple.getTimestamp() < 1) {
						wrongTuples ++;
						continue;
					}
					
					totalTuples += 1;
				
					/* Populate data */
					data.putLong  (tuple.getTimestamp() );
					data.putLong  (tuple.getJobId()     );
					data.putLong  (tuple.getTaskId()    );
					data.putLong  (tuple.getMachineId() );
					data.putInt   (tuple.getEventType() );
					data.putInt   (tuple.getUserId()    );
					data.putInt   (tuple.getCategory()  );
					data.putInt   (tuple.getPriority()  );
					data.putFloat (tuple.getCpu()       );
					data.putFloat (tuple.getRam()       );
					data.putFloat (tuple.getDisk()      );
					data.putInt   (tuple.getConstraint());
					
					++tuple_counter;
					if (data.remaining() == 0) {
						/* Assert that tuple counter equals bundle size */
						if (tuple_counter != bundle) {
							System.err.println("error: invalid bundle size");
							System.exit(1);
						}
						/* Compress the data */
						compressed = Utils.compress(data.array());
						compressedBytes += compressed.length;
						ByteBuffer buffer = ByteBuffer.wrap(compressed);
						bundles.add(buffer);
						/* Reset state */
						data.clear();
						tuple_counter = 0L;
					}
				}
				
				d.close();
			}
			
			dt = (double ) (System.currentTimeMillis() - start) / 1000.;
			/* Stats */
			rate =  (double) (lines) / dt;
			MBps = ((double) bytes / _1MB) / dt;
			
			System.out.println();
			System.out.println(String.format("[DBG] %12d lines read", lines));
			System.out.println(String.format("[DBG] %12d bytes read", bytes));
			System.out.println(String.format("[DBG] %12d tuples", totalTuples));
			System.out.println();
			System.out.println(String.format("[DBG] %12d compressed bytes", compressedBytes));
			System.out.println(String.format("[DBG] %12d bundles", bundles.size()));
			System.out.println();
			/* Statistics */
			System.out.println(String.format("[DBG] %12d wrong tuples", wrongTuples));
			System.out.println(String.format("[DBG] %12d tuples missing info", missing));
			System.out.println(String.format("[DBG] %12d invalid tuples", invalid));
			System.out.println();
			System.out.println(String.format("[DBG] %10.1f seconds", (double) dt));
			System.out.println(String.format("[DBG] %10.1f tuples/s", rate));
			System.out.println(String.format("[DBG] %10.1f MB/s", MBps));
			System.out.println();
			
			/* Send data */
			bytes = 0L;
			
			int _bundle = tupleSize * bundle;
			/* Launch worker threads */
			Worker [] workers = new Worker [NTHREADS];
			ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
			
			int iterations = 1;
			
			for (int idx = 0; idx < workers.length; idx++) {
				workers[idx] = new Worker (bundles, idx, workers.length, _bundle, iterations, 2505600);
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

