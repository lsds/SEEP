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
		return MultiOpTuple.newInstance(objects, timestamp, timestamp);
	}
	
	public static void main (String [] args) {
		
		if (args.length != 1) {
			System.err.println("usage: java LRBRunner [input filename]");
			System.exit(1);
		}
		
		FileInputStream f;
		DataInputStream d;
		BufferedReader  b;
		
		String line = null;
		long lines = 0;
		
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

		try {
			f = new FileInputStream(args[0]);
			d = new DataInputStream(f);
			b = new BufferedReader(new InputStreamReader(d));
			
			System.out.println("Loading file...");
			start = System.currentTimeMillis();
			long lastTupleTimestamp = 0;
			while ((line = b.readLine()) != null) {
				lines += 1;
				bytes += line.length() +1; /* +1 for '\n' */

				if (Integer.valueOf(line.split(",")[8]) < 0){
					wrongtuples += 1;
					continue;
				}

				if (Integer.valueOf(line.split(",")[0]) != 0){
					continue;
				}

				MultiOpTuple t = parseLine(line);
				data.add(t);
				lastTupleTimestamp = t.timestamp;
				
				if (lines%1000000 == 0)
					System.out.println(String.format("%10d - %10d", System.currentTimeMillis(), lines));
				
			}
			dt = (double ) (System.currentTimeMillis() - start) / 1000.;
			d.close();
			/* Stats */
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
			
			NullAPI api = new NullAPI();
			api.waitForInstrumentationTimestamp = lastTupleTimestamp;
			api.totalTuples = totalTuples;
			
			LRBQ4 query = new LRBQ4();
			query.setup(api);
			
			/* Q4 */
			System.out.println("Q4 computations " + System.currentTimeMillis());
			api.startTimestamp = System.currentTimeMillis(); /* End-to-end measurement */
			for (MultiOpTuple t: data) {
				query.process(t);
			}
			
		} catch (Exception e) { System.err.println(e.getMessage()); }
		
	}
	
}
