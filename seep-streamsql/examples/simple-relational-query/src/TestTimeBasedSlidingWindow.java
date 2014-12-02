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
			new WindowDefinition (WindowDefinition.WindowType.ROW_BASED, Utils.RANGE, Utils.SLIDE);
		
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
		while (b.hasRemaining()) {
			b.putLong(timestamp);
			for (int i = 0; i < Utils._TUPLE_ - Long.SIZE; i += Integer.SIZE)
				b.putInt(1);
			if (++tuples % 1024 == 0)
				timestamp += 1;
		}
		b.flip();
		try {
			while (true) {
				operator.processData (data);
				b.reset();
				/* Increment timestamps */
				/* ... */
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
