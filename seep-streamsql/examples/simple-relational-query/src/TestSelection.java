import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntAddition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntSubtraction;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless.ProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless.SelectionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestSelection {

	public static void main(String [] args) {
		
		WindowDefinition window = 
			new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		/*
		IPredicate [] predicates = new IPredicate [4];
		
		predicates[0] = new IntComparisonPredicate(IntComparisonPredicate.LESS_OP, new IntColumnReference(1), 
				new IntAddition (new IntColumnReference(2), new IntConstant(1))
		);
		
		predicates[1] = new IntComparisonPredicate(IntComparisonPredicate.GREATER_OP, new IntColumnReference(1), 
				new IntSubtraction (new IntColumnReference(2), new IntConstant(1))
		);
		
		predicates[2] = new IntComparisonPredicate(IntComparisonPredicate.LESS_OP, new IntColumnReference(3), 
				new IntAddition (new IntColumnReference(4), new IntConstant(1))
		);
		
		predicates[3] = new IntComparisonPredicate(IntComparisonPredicate.GREATER_OP, new IntColumnReference(4), 
				new IntSubtraction (new IntColumnReference(4), new IntConstant(1))
		);
		
		IMicroOperatorCode selectionCPUCode = new Selection (
			new ANDPredicate (predicates)
		);
		*/
		
		IPredicate [] predicates = new IPredicate [1];
		for (int i = 0; i < predicates.length; i++) {
			int j = i % 6 + 1;
			predicates[i] = new IntComparisonPredicate(IntComparisonPredicate.LESS_OP, new IntColumnReference(j), new IntConstant(i + 2));
		}
		
		IMicroOperatorCode selectionCPUCode = new Selection (
				new ANDPredicate(predicates)
		);
		
		MicroOperator uoperator = new MicroOperator (selectionCPUCode, null, 1);
		
		/* Query */
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window);
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
