import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.LinkedList;

import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;

// import uk.ac.imperial.lsds.seep.gpu.types.*;


public class LRBRunner implements Callback {
	
//	type         = Integer.parseInt(s[ 0]);
//	timestamp    = Integer.parseInt(s[ 1]);
//	vehicleId    = Integer.parseInt(s[ 2]);
//	speed        = Integer.parseInt(s[ 3]);
//	highway      = Integer.parseInt(s[ 4]);
//	lane         = Integer.parseInt(s[ 5]);
//	direction    = Integer.parseInt(s[ 6]);
//	segment      = Integer.parseInt(s[ 7]);
//	position     = Integer.parseInt(s[ 8]);
//	queryId      = Integer.parseInt(s[ 9]);
//	startSegment = Integer.parseInt(s[10]);
//	endSegment   = Integer.parseInt(s[11]);
//	weekday      = Integer.parseInt(s[12]);
//	minute       = Integer.parseInt(s[13]);
//	day          = Integer.parseInt(s[14]);

	private static Object hardCodedCast(int i, String value) {
		switch (attributes[i].split(":")[1]) {
		case "Integer":
			return new IntegerType(Integer.valueOf(value));
		case "Float":
			return new FloatType(Float.valueOf(value));
		default:
			return value;
		}
	}

	private static String[] attributes = {
		"type:Integer",
		"timestamp:Integer",
		"vehicleId:Integer",
		"speed:Float" ,
		"highway:Integer",
		"lane:Integer",
		"direction:Integer",
		"segment:Integer",
		"position:Integer",
		"queryId:Integer",
		"startSegment:Integer",
		"endSegment:Integer",
		"weekday:Integer",
		"minute:Integer",
		"day:Integer"
		};
	
	// INPUT STREAM vehicleID, speed, highway, direction, position
	private static int[] includeAttributes = {
		2,3,4,6,8
	};
	
	private static MultiOpTuple parseLine(String line) {
		String [] s = line.split(",");
		Object[] objects = new Object[includeAttributes.length];
		for (int i = 0; i < includeAttributes.length; i++)
			objects[i] = hardCodedCast(includeAttributes[i],s[includeAttributes[i]]);
		
		long timestamp = Long.valueOf(s[1]);
		return new MultiOpTuple(objects, timestamp, timestamp);
	}
	
	public static void main (String [] args) {
		
		if (args.length != 2) {
			System.err.println("usage: java LRBRunner [input filename] [L]");
			System.exit(1);
		}
		System.out.println("Application starts at " + System.currentTimeMillis());
		
		// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		FileInputStream f;
		DataInputStream d;
		BufferedReader  b;
		
		String line = null;
		long lines = 0;
		long MAX_LINES = 12048577L;
        long percent_ = 0L, _percent = 0L;
		
		Deque<MultiOpTuple> data = new LinkedList<>();
		
		/* Time measurements */
		long start = 0L;
		long bytes = 0L;
		double dt;
		double rate; /* tuples/sec */
		double _1MB = 1024. * 1024.;
		double MBps; /* MB/sec */
		int totalTuples;
		
		long wrongtuples = 0L;
		
		int L = Integer.parseInt(args[1]);
		System.out.println("L=" + L);
		
		try {
			
			f = new FileInputStream(args[0]);
			d = new DataInputStream(f);
			b = new BufferedReader(new InputStreamReader(d));
			
			start = System.currentTimeMillis();
			long lastTupleTimestamp = 0;
			long dummy_lastTupleTimestamp = 0;
			
			long dummy_ts = 0;
			long tuple_counter = 0;
			
			while ((line = b.readLine()) != null && lastTupleTimestamp < 3000) { // 10500) {
				lines += 1;
				bytes += line.length() +1; // +1 for '\n'
				
				percent_ = (lines * 100) / MAX_LINES;
				if (percent_ == (_percent + 1)) {
					System.out.print(String.format("Loading file...%3d%%\r", percent_));
					_percent = percent_;
				}

				if (Integer.valueOf(line.split(",")[8]) < 0){
					wrongtuples += 1;
					continue;
				}

				if (Integer.valueOf(line.split(",")[0]) != 0){
					continue;
				}

				MultiOpTuple t = parseLine(line);
				// pos to segment translation
				t.values[4] = ((IntegerType)t.values[4]).div(new IntegerType(5280));
				lastTupleTimestamp = t.timestamp;
				
				t.timestamp = dummy_ts;
				t.instrumentation_ts = dummy_ts;
				
				data.add(t);
				
				tuple_counter += 1;
				if (tuple_counter % 42 == 0)
					dummy_ts += 1;
				
				MultiOpTuple copy;
				Object [] v;
                for (int i = 1; i < L; i++) {
					v = new Object [t.values.length];
					System.arraycopy(t.values, 0, v, 0, t.values.length);
                    copy = new MultiOpTuple (v, t.timestamp, t.instrumentation_ts);
                    copy.values[2] = new IntegerType(i); /* L = 1, 2, 3,... */
                    data.add(copy);
                }
				dummy_lastTupleTimestamp = t.timestamp;
				
			}
			dt = (double ) (System.currentTimeMillis() - start) / 1000.;
			d.close();
			// Stats
			rate =  (double) (lines) / dt;
			MBps = ((double) bytes / _1MB) / dt;
			totalTuples = data.size();
			
			System.out.println(String.format("%10d lines read", lines));
			System.out.println(String.format("%10d bytes read", bytes));
			System.out.println(String.format("%10d tuples in deque", totalTuples));
			System.out.println();
			System.out.println(String.format("%10.1f seconds", (double) dt));
			System.out.println(String.format("%10.1f tuples/s", rate));
			System.out.println(String.format("%10.1f MB/s", MBps));
			System.out.println(String.format("%10d tuples ignored", wrongtuples));
			System.out.println();
			
			lastTupleTimestamp--;
			NullAPI api = new NullAPI();
			api.waitForInstrumentationTimestamp = lastTupleTimestamp;
			api.totalTuples = totalTuples;
			
			LRBQ4 query = new LRBQ4();
			// LRBQ1To6 query = new LRBQ1To6();
			query.setup(api);
			
			/* Q4 */
			System.out.println("Q4 computations " + System.currentTimeMillis());
			// System.out.println("will be done when receiving tuple with timestamp " + lastTupleTimestamp);
			api.startTimestamp = System.currentTimeMillis(); /* End-to-end measurement */
			
			/* Previous loop */
			// for (MultiOpTuple t: data) {
			//	query.process(t);
			//}
			
			MultiOpTuple t;
			while (true) {
				t = data.poll();
				query.process(t);
				t.timestamp += dummy_lastTupleTimestamp;
				t.instrumentation_ts += dummy_lastTupleTimestamp;
				data.add(t);
			}
			
		} catch (Exception e) { System.err.println(e.getMessage()); }
	}
	
}
