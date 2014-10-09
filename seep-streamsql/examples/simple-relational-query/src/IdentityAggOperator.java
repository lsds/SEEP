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
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.Division;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
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


public class IdentityAggOperator {

	private MultiOperator mo;
	
	private MultiAPI api;
	
	public void setup(MultiAPI api) {
		this.api = api;
		

		Map<Integer, IWindowDefinition> windowDefs = new HashMap<>();
		
		// INPUT STREAM vehicleId, speed, segNo, dir, hwy
		/*
		 * Query 4
		 * 
		 * Select segNo, dir, hwy
		 * From SegSpeedStr [Range 5 Minutes]
		 * Group By segNo, dir, hwy
		 * Having Avg(speed) < 40
		 */
		Selection having = new Selection(
				new ComparisonPredicate<FloatType>(
				ComparisonPredicate.LESS_OP, 
				new ColumnReference<FloatType>(3), 
				new Constant<FloatType>(new FloatType(40f))));
				
		@SuppressWarnings("unchecked")
		IMicroOperatorCode q4AggCode = new MicroAggregation(
				AggregationType.AVG, 
				new ColumnReference<PrimitiveType>(1),
				(ColumnReference<PrimitiveType>[]) new ColumnReference[] {
					new ColumnReference<IntegerType>(2),
					new ColumnReference<FloatType>(3),
					new ColumnReference<IntegerType>(4)
					},
				having
				);
		
		IMicroOperatorConnectable q4Agg = QueryBuilder.newMicroOperator(q4AggCode, 1003);

//		@SuppressWarnings("unchecked")
//		IMicroOperatorCode q4ProjCode = new Projection((IValueExpression<PrimitiveType>[]) new IValueExpression[] {
//				new ColumnReference<IntegerType>(0),
//				new ColumnReference<IntegerType>(1),
//				new ColumnReference<IntegerType>(2)
//				});
//		
//		IMicroOperatorConnectable q4Proj = QueryBuilder.newMicroOperator(q4ProjCode, 1002);

//		q4Agg.connectTo(1000, q4Proj);

		Set<IMicroOperatorConnectable> q4MicroOps = new HashSet<>();
//		q4MicroOps.add(q4Proj);
		q4MicroOps.add(q4Agg);

		windowDefs = new HashMap<>();
		windowDefs.put(100, new WindowDefinition(WindowType.RANGE_BASED, 300, 1));
		ISubQueryConnectable sq4 = QueryBuilder.newSubQuery(q4MicroOps, 103, windowDefs);

		Set<ISubQueryConnectable> subQueries = new HashSet<>();
		subQueries.add(sq4);

		this.mo = MultiOperator.synthesizeFrom(subQueries, 1);
		this.mo.setUp();
	}
	
	public void process(MultiOpTuple tuple) {
		this.mo.processData(tuple, this.api);
	}
}

