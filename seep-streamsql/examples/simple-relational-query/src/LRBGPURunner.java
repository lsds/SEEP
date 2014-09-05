import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.LinkedList;

import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

/* import uk.ac.imperial.lsds.seep.gpu.types.*; */
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;

public class LRBGPURunner implements Callback {

	private static Object hardCodedCast (int i, String value) {
		
		switch (attributes[i].split(":")[1]) {
			case "Integer": 
				return new IntegerType(Integer.valueOf(value));
			case "Float":
				return new FloatType(Float.valueOf(value));
			default: return value;
		}
	}
	
	private static String [] attributes = {
		
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
	
	/* Input stream is `vehicleId`, `speed`, `highway`, `direction`, and `position` */
	private static int [] includeAttributes = { 2, 3, 4, 6, 8 };
	
	private static MultiOpTuple parseLine(String line) {
        String [] s = line.split(",");
        Object[] objects = new Object[includeAttributes.length];
        for (int i = 0; i < includeAttributes.length; i++)
            objects[i] = hardCodedCast(includeAttributes[i],s[includeAttributes[i]]);

        long timestamp = Long.valueOf(s[1]);
        return new MultiOpTuple(objects, timestamp, timestamp);
    }
	
	public static void main (String [] args) {
		
		if (args.length != 1) {
			System.err.println("usage: java LRBGPURunner [input filename]");
			System.exit(1);
		}
		
		FileInputStream f;
		DataInputStream d;
		BufferedReader  b;
		
		String line = null;
		long lines = 0;
		long MAX_LINES = 12048577L;
        long percent_ = 0L, _percent = 0L;
		
		Deque<MultiOpTuple> data = new LinkedList<MultiOpTuple>();
		
		/* Time measurements */
		long start = 0L;
		long bytes = 0L;
		double dt;
		double rate; /* tuples/sec */
		double _1MB = 1024. * 1024.;
		double MBps; /* MB/sec */
		int totalTuples;
		
		long wrongtuples = 0L;
		
		LRBQ4GPU query = new LRBQ4GPU();
		
		try {
			
			f = new FileInputStream(args[0]);
			d = new DataInputStream(f);
			b = new BufferedReader(new InputStreamReader(d));
			
			start = System.currentTimeMillis();
			long lastTupleTimestamp = 0;
			
			while ((line = b.readLine()) != null && lastTupleTimestamp < 10500) {
				lines += 1;
				bytes += line.length() +1; // +1 for '\n'
				
				percent_ = (lines * 100) / MAX_LINES;
				if (percent_ == (_percent + 1)) {
					System.out.print(String.format("Loading file...%3d%%\r", percent_));
					_percent = percent_;
				}
				
				if (Integer.valueOf(line.split(",")[8]) < 0) {
					wrongtuples += 1;
					continue;
				}
				
				if (Integer.valueOf(line.split(",")[0]) != 0) {
					continue;
				}
				
				MultiOpTuple t = parseLine(line);
				/* `position` to segment translation */
				t.values[4] = new IntegerType((int)Math.floor(((IntegerType)t.values[4]).value / 5280));
				
				data.add(t);
				lastTupleTimestamp = t.timestamp;
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
			System.out.println();
			
			lastTupleTimestamp--;
			NullAPI api = new NullAPI ();
			api.waitForInstrumentationTimestamp = lastTupleTimestamp;
			api.totalTuples = totalTuples;
			
			query.setup(api);
			
			/* Q4 */
			System.out.println("Q4 computations ");
			api.startTimestamp = System.currentTimeMillis(); /* End-to-end measurement */
			for (MultiOpTuple t: data) {
				query.process(t);
			}
			
		} catch (Exception e) { System.err.println(e.getMessage()); }
	}
}
