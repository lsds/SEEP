import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.File;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.ArrayList;

public class TaskEventCompressor {
	
	private static final String usage = "usage: java TaskEventCompressor";
	
	public static void main (String [] args) {
		
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
		
		/* Parse command line arguments */
		int i, j;
		for (i = 0; i < args.length; ) {
			if ((j = i + 1) == args.length) {
				System.err.println(usage);
				System.exit(1);
			}
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
					data.putLong  (tuple.getTimestamp() );//  0 
					data.putLong  (tuple.getJobId()     );//  8
					data.putLong  (tuple.getTaskId()    );// 16
					data.putLong  (tuple.getMachineId() );// 24
					data.putInt   (tuple.getEventType() );// 32
					data.putInt   (tuple.getUserId()    );// 36
					data.putInt   (tuple.getCategory()  );// 40
					data.putInt   (tuple.getPriority()  );// 44
					data.putFloat (tuple.getCpu()       );// 48
					data.putFloat (tuple.getRam()       );// 52
					data.putFloat (tuple.getDisk()      );// 56
					data.putInt   (tuple.getConstraint());// 60
					
					++tuple_counter;
					if (data.remaining() == 0) {
						/* Assert that tuple counter equals bundle size */
						if (tuple_counter != bundle) {
							System.err.println("error: invalid bundle size");
							System.exit(1);
						}
						/* Compress the data */
						compressed = SmartGridUtils.compress(data.array());
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
			/* Statistics */
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
			
			/* Writing compressed data to file */
			System.out.println("[DBG] writing compressed data...");
			
			File datafile = new File (String.format("compressed-%d-norm.dat", bundle));
			FileOutputStream f_ = new FileOutputStream (datafile);
			BufferedOutputStream output_ = new BufferedOutputStream(f_);
			long written = 0L;
			long offsets = 0L;
			
			for (ByteBuffer buffer: bundles) {
				int length = buffer.array().length;
				ByteBuffer L = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
				L.putInt(length);
				output_.write(L.array());
				output_.write(buffer.array());
				output_.flush();
				written += length;
				offsets += L.array().length;
			}
			output_.close();
			System.out.println(String.format("[DBG] %12d compressed bytes written (%d)", 
				written, (written + offsets)));
			
			System.out.println("Bye.");
		
		} catch (Exception e) {
			System.err.println(String.format("error: %s", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
		}
	}
}

