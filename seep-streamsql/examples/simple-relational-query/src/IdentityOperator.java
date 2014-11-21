import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.operator.MultiAPI;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.eint.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IValueExpression;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroPaneAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;


public class IdentityOperator {

	private MultiOperator operator;
	
	private MultiAPI api;
	
	public void setup(MultiAPI api) {
		this.api = api;
		
		/*
		 * Identity query:
		 * 
		 * Select * from [stream] where speed < 0
		 *
		 * Returns an empty set.
		 */
		
		Map<Integer, IWindowDefinition> windowDefs = new HashMap<Integer, IWindowDefinition>();
		
		/* The input stream consists of the following attributes:
 		 * vehicleID, speed, highway, direction, position
 		 */
		
		IPredicate [] band = new IPredicate [2];
		band[0] = new ComparisonPredicate<FloatType>(ComparisonPredicate.GREATER_OP, new ColumnReference<FloatType>(1), new Constant<FloatType>(new FloatType(  -1f)));
		band[1] = new ComparisonPredicate<FloatType>(ComparisonPredicate.LESS_OP,    new ColumnReference<FloatType>(1), new Constant<FloatType>(new FloatType(1000f)));
		// band[2] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.GREATER_OP, new ColumnReference<IntegerType>(2), new Constant<IntegerType>(new IntegerType( -10)));
		// band[3] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.LESS_OP,    new ColumnReference<IntegerType>(2), new Constant<IntegerType>(new IntegerType(20000)));
		// band[4] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.GREATER_OP, new ColumnReference<IntegerType>(3), new Constant<IntegerType>(new IntegerType(  -1)));
		// band[5] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.LESS_OP,    new ColumnReference<IntegerType>(3), new Constant<IntegerType>(new IntegerType(10000)));
		// band[6] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.GREATER_OP, new ColumnReference<IntegerType>(4), new Constant<IntegerType>(new IntegerType( -10)));
		// band[7] = new ComparisonPredicate<IntegerType>(ComparisonPredicate.LESS_OP,    new ColumnReference<IntegerType>(4), new Constant<IntegerType>(new IntegerType(20000)));
		
		ANDPredicate select_predicate = new ANDPredicate(band);
		System.out.println(select_predicate);
		
		@SuppressWarnings("unchecked")
		IMicroOperatorCode IdOpCode = new Selection(select_predicate);
		/*
		@SuppressWarnings("unchecked")
		IMicroOperatorCode IdOpCode = new Selection
			(
				new ComparisonPredicate<FloatType>
				(
					ComparisonPredicate.GREATER_OP,
					new ColumnReference<FloatType>(1),
					new Constant<FloatType>(new FloatType(-1f))
				)
			);
		*/
		IMicroOperatorConnectable IdOp = QueryBuilder.newMicroOperator(IdOpCode, 3);
		
		Set<IMicroOperatorConnectable> IdMicroOps = new HashSet<IMicroOperatorConnectable>();
		IdMicroOps.add(IdOp);
		
		windowDefs.put(12, new WindowDefinition(WindowType.RANGE_BASED, 0, 1));
		ISubQueryConnectable sq = QueryBuilder.newSubQuery(IdMicroOps, 5, windowDefs);
		Set<ISubQueryConnectable> subQueries = new HashSet<ISubQueryConnectable>();
		subQueries.add(sq);
		
		this.operator = MultiOperator.synthesizeFrom(subQueries, 100);
		this.operator.setUp();
	}
	
	public void process(MultiOpTuple tuple) {
		this.operator.processData(tuple, this.api);
	}
}

