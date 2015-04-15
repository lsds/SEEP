import java.io.File;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class TaskEventAnalysis {
	
	private static final String usage = "usage: java TaskEventCompressor";
	
	public static void main (String [] args) {
		
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
		
		TreeMap<Long,Long> histogram = new TreeMap<Long,Long>();
		
		TaskEventTuple tuple = new TaskEventTuple ();
		
		try {
			
			start = System.currentTimeMillis();
			
			/* From the Google cluster data README file:
			 * 
			 * "Each record has a time stamp, which is in microseconds since  600 seconds 
			 * before the beginning of the trace period, and recorded as a 64-bit integer 
			 * (i.e., an event 20 seconds after the start of the trace would have a time-
			 * stamp = 620s).
			 */
			long __ts_init = 0; /* 600 secs */
			long __ts_prev = __ts_init;
			long __ts_curr = __ts_prev;
			
			long __ts_step = 1; /* 1 sec */
			long __ts_next = __ts_init + __ts_step;
			
			long tuple_counter = 0L;
			
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
					
					__ts_curr = tuple.getTimestamp();
					
					if (__ts_curr < __ts_prev) {
						System.err.println("fatal error: out-of-order event");
						System.exit(1);
					}
					
					/* Build histogram (events/second) */
					
					/* System.out.println(String.format("%03d: ts %16d us", totalTuples, tuple.getTimestamp())); */
					
					while (__ts_curr >= __ts_next) {
						/* Store <__ts_init, tuple_counter> */
						histogram.put(__ts_init, tuple_counter);
						/* Reset */
						tuple_counter = 0;
						__ts_init = __ts_next;
						__ts_next = __ts_init + __ts_step;
					}
					
					tuple_counter ++;
					
					__ts_prev = __ts_curr;
				}
				
				d.close();
			}
			
			System.out.println();
			System.out.println(String.format("[DBG] %12d is the last observed timestamp", __ts_curr));
			
			dt = (double ) (System.currentTimeMillis() - start) / 1000.;
			/* Statistics */
			rate =  (double) (lines) / dt;
			MBps = ((double) bytes / _1MB) / dt;
			
			System.out.println();
			System.out.println(String.format("[DBG] %12d lines read", lines));
			System.out.println(String.format("[DBG] %12d bytes read", bytes));
			System.out.println(String.format("[DBG] %12d tuples", totalTuples));
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
			/* Histogram */
			for(Map.Entry<Long,Long> entry : histogram.entrySet()) {
				Long k = entry.getKey();
				Long v = entry.getValue();
				System.out.println(String.format("%16d %16d", k.longValue(), v.longValue()));
			}
			
			System.out.println("Bye.");
		
		} catch (Exception e) {
			System.err.println(String.format("error: %s", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
		}
	}
}

