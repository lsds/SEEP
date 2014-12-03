import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class TestTimeBasedSlidingWindow {

	public static void main(String [] args) {
		
		WindowDefinition window = 
			new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		
		/* Query */
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window);
		queries.add(query);
			
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		byte [] data = new byte [Utils.BUNDLE];
		ByteBuffer b = ByteBuffer.wrap(data);
		long timestamp = 0L;
		long tuples = 0L;
		/* Utils.BUNDLE holds 32,768 tuples; or, 
		 * given 32KB/s, 32 seconds of data */
		long tps = 1024L;
		while (b.hasRemaining()) {
			b.putLong(timestamp);
			for (int i = 0; i < Utils._TUPLE_ - Long.SIZE; i += Integer.SIZE)
				b.putInt(1);
			if (++tuples % tps == 0)
				timestamp += 1;
		}
		assert (timestamp == 32);
		timestamp --;
		b.flip();
		try {
			while (true) {
				operator.processData (data);
				b.clear();
				/* Increment timestamps */
				for (int i = 0; i < Utils.BUNDLE; i += Utils._TUPLE_)
					b.putLong(i, b.getLong(i) + timestamp);
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
