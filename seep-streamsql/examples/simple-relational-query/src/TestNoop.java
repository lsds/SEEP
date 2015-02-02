import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.op.stateless.Noop;

public class TestNoop {

	public static void main(String [] args) {
		
		WindowDefinition window = 
			new WindowDefinition (TestUtils.TYPE, TestUtils.RANGE, TestUtils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (TestUtils.OFFSETS, TestUtils._TUPLE_);
		
		IMicroOperatorCode noop = new Noop ();
		
		MicroOperator uoperator = new MicroOperator (noop, 1);
		
		/* Query */
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, new QueryConf(200, 1024));
		queries.add(query);
			
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		byte [] data = new byte [Utils.BUNDLE];
		ByteBuffer b = ByteBuffer.wrap(data);
		while (b.hasRemaining())
			b.putInt(1);
		try {
			while (true) {
				operator.processData (data);
				/* Thread.sleep(1000L); */
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
