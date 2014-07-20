import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.conversion.DoubleConversion;
import uk.ac.imperial.lsds.streamsql.conversion.IntegerConversion;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.Division;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;


public class LocalBaseRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<String> posSpeedStr = new ArrayList<String>();
		posSpeedStr.add("vehicleId");
		posSpeedStr.add("speed");
		posSpeedStr.add("xPos");
		posSpeedStr.add("dir");
		posSpeedStr.add("hwy");
		
		/*
		 * Query 1
		 * 
		 * Select vehicleId, speed, xPos/1760 as segNo, dir, hwy
		 * From PosSpeedStr
		 */
		List<IValueExpression> projExpressions = new ArrayList<>();
		projExpressions.add(new ColumnReference<>(new StringConversion(), "vehicleId"));
		projExpressions.add(new ColumnReference<>(new StringConversion(), "speed"));
		IValueExpression ex = new Division(new ColumnReference<>(new DoubleConversion(), "xPos"), new ValueExpression<>(new IntegerConversion(), 1760));
		projExpressions.add(ex);
		projExpressions.add(new ColumnReference<>(new StringConversion(), "dir"));
		projExpressions.add(new ColumnReference<>(new StringConversion(), "hwy"));
		
		IMicroOperatorCode q1ProjCode = new Projection(projExpressions);
		IMicroOperatorConnectable q1Proj = QueryBuilder.newMicroOperator(q1ProjCode, 1, posSpeedStr);
		
		Set<IMicroOperatorConnectable> q1MicroOps = new HashSet<>();
		q1MicroOps.add(q1Proj);

		Map<Integer, IWindowDefinition> windowDefs = new HashMap<>();
		windowDefs.put(11, new WindowDefinition(WindowType.ROW_BASED, 1, 1));
		ISubQueryConnectable sq1 = QueryBuilder.newSubQuery(q1MicroOps, 2, posSpeedStr, windowDefs);

		/* 
		 * currently not used since idxMapper of datatuple is not
		 * set correctly in local mode
		 */
		List<String> segSpeedStr = new ArrayList<String>();
		segSpeedStr.add("vehicleId");
		segSpeedStr.add("speed");
		segSpeedStr.add("segNo");
		segSpeedStr.add("dir");
		segSpeedStr.add("hwy");
		
		/*
		 * Query 2
		 * 
		 * Select segNo, dir, hwy
		 * From SegSpeedStr [Range 5 Minutes]
		 * Group By segNo, dir, hwy
		 * Having Avg(speed) < 40
		 */
		IMicroOperatorCode q2ProjCode = new Projection(new String[] {"xPos", "dir", "hwy"});
		IMicroOperatorConnectable q2Proj = QueryBuilder.newMicroOperator(q2ProjCode, 2, posSpeedStr);
		IMicroOperatorCode q2AggCode = new MicroAggregation(MicroAggregation.AggregationType.AVG, "speed", new String[] {"xPos", "dir", "hwy"});
		IMicroOperatorConnectable q2Agg = QueryBuilder.newMicroOperator(q2AggCode, 3, posSpeedStr);

		IMicroOperatorCode q2SelCode = new Selection(new ComparisonPredicate(
				ComparisonPredicate.LESS_OP, 
				new ColumnReference<>(new StringConversion(), "AVG(speed)"), 
				new ValueExpression<>(new IntegerConversion(), 40)));
		IMicroOperatorConnectable q2Sel = QueryBuilder.newMicroOperator(q2SelCode, 4, posSpeedStr);
		
		q2Agg.connectTo(1, q2Sel);
		q2Sel.connectTo(2, q2Proj);

		Set<IMicroOperatorConnectable> q2MicroOps = new HashSet<>();
		q2MicroOps.add(q2Proj);
		q2MicroOps.add(q2Agg);
		q2MicroOps.add(q2Sel);

		windowDefs = new HashMap<>();
		windowDefs.put(12, new WindowDefinition(WindowType.RANGE_BASED, 300, 1));
		ISubQueryConnectable sq2 = QueryBuilder.newSubQuery(q1MicroOps, 5, posSpeedStr, windowDefs);

		sq1.connectTo(sq2, 101);

		Set<ISubQueryConnectable> subQueries = new HashSet<>();
		subQueries.add(sq1);
		subQueries.add(sq2);
		
//		Connectable multiOp = QueryBuilder.newMultiOperator(subQueries, 100, posSpeedStr);

		MultiOperator mo = MultiOperator.synthesizeFrom(subQueries, 100);
		mo.setUp();

		/*
		 * Send data
		 */
		FileAPI api = new FileAPI("./myOut.csv", posSpeedStr);
		
		Map<String, Integer> mapper = new HashMap<>();
		for (int i = 0; i < posSpeedStr.size(); i++)
			mapper.put(posSpeedStr.get(i), i);
		
		DataTuple data = new DataTuple(mapper, new TuplePayload());

		DataTuple output = data.newTuple(1, 55, 2, -1, 42);
		DataTuple out1 = output;
		mo.processData(output, api);
		output = data.newTuple(2, 40, 13, 1, 42);
		output.getPayload().timestamp++;
		mo.processData(output, api);
		
		
	}

}
