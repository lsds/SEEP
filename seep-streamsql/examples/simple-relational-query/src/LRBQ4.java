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
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;


public class LRBQ4 {

	private MultiOperator mo;
	
	private MultiAPI api;
	
	public void setup(MultiAPI api) {
		this.api = api;
		
		/*
		 * Query 1
		 * 
		 * Select vehicleId, speed, xPos/5280 as segNo, dir, hwy
		 * From PosSpeedStr
		 */
//		List<IValueExpression> projExpressions = new ArrayList<>();
//		projExpressions.add(new ColumnReference<>(new StringConversion(), "vehicleId"));
//		projExpressions.add(new ColumnReference<>(new StringConversion(), "speed"));
//		IValueExpression ex = new Division(new ColumnReference<>(new DoubleConversion(), "xPos"), new ValueExpression<>(new IntegerConversion(), 5280));
//		projExpressions.add(ex);
//		projExpressions.add(new ColumnReference<>(new StringConversion(), "dir"));
//		projExpressions.add(new ColumnReference<>(new StringConversion(), "hwy"));
//		
//		IMicroOperatorCode q1ProjCode = new Projection(projExpressions, segSpeedStr);
//		IMicroOperatorConnectable q1Proj = QueryBuilder.newMicroOperator(q1ProjCode, 1);
//		
//		Set<IMicroOperatorConnectable> q1MicroOps = new HashSet<>();
//		q1MicroOps.add(q1Proj);
//
		Map<Integer, IWindowDefinition> windowDefs = new HashMap<>();
//		windowDefs.put(11, new WindowDefinition(WindowType.ROW_BASED, 1, 1));
//		ISubQueryConnectable sq1 = QueryBuilder.newSubQuery(q1MicroOps, 2, windowDefs);


		// INPUT STREAM vehicleID, speed, highway, direction, position

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
		IMicroOperatorCode q2AggCode = new MicroAggregation(
				MicroAggregation.AggregationType.AVG, 
				new ColumnReference<PrimitiveType>(1),
				(ColumnReference<PrimitiveType>[]) new ColumnReference[] {
					new ColumnReference<IntegerType>(2),
					new ColumnReference<FloatType>(3),
					new ColumnReference<IntegerType>(4)
					},
				having,
				211
				);
		
		IMicroOperatorConnectable q2Agg = QueryBuilder.newMicroOperator(q2AggCode, 3);

		//@SuppressWarnings("unchecked")
		//IMicroOperatorCode q2ProjCode = new Projection((IValueExpression<PrimitiveType>[]) new IValueExpression[] {
		//		new ColumnReference<IntegerType>(0),
		//		new ColumnReference<IntegerType>(1),
		//		new ColumnReference<IntegerType>(2)
		//		});
		
		//IMicroOperatorConnectable q2Proj = QueryBuilder.newMicroOperator(q2ProjCode, 5);

		//q2Agg.connectTo(1, q2Proj);

		Set<IMicroOperatorConnectable> q2MicroOps = new HashSet<>();
		//q2MicroOps.add(q2Proj);
		q2MicroOps.add(q2Agg);

		windowDefs = new HashMap<>();
		windowDefs.put(12, new WindowDefinition(WindowType.RANGE_BASED, 300, 1));
//		windowDefs.put(12, new WindowDefinition(WindowType.RANGE_BASED, 2, 1));
		ISubQueryConnectable sq2 = QueryBuilder.newSubQuery(q2MicroOps, 5, windowDefs);

//		sq1.connectTo(sq2, 101);

		Set<ISubQueryConnectable> subQueries = new HashSet<>();
//		subQueries.add(sq1);
		subQueries.add(sq2);
		
//		Connectable multiOp = QueryBuilder.newMultiOperator(subQueries, 100, posSpeedStr);

		this.mo = MultiOperator.synthesizeFrom(subQueries, 100);
		this.mo.setUp();

	}
	
	public void process(MultiOpTuple tuple) {
		this.mo.processData(tuple, this.api);
	}
	
}
