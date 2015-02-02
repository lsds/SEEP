import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ASelectionKernel;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestSelectionKernel {

	public static void main(String [] args) {
		
		String filename = args[0];
		
		WindowDefinition window = 
			new WindowDefinition (TestUtils.TYPE, TestUtils.RANGE, TestUtils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (TestUtils.OFFSETS, TestUtils._TUPLE_);
		
		ASelectionKernel selectionCode = new ASelectionKernel (
			new IntComparisonPredicate (IntComparisonPredicate.LESS_OP, new IntColumnReference(1), new IntConstant(40)),
			schema,
			filename
		);
		/* System.out.println(String.format("[DBG] %s", selectionCode)); */
		
		MicroOperator uoperator = new MicroOperator (selectionCode, null, 1);
		
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
		b.order(ByteOrder.LITTLE_ENDIAN);
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

