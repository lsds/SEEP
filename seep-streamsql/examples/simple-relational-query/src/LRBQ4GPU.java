import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.operator.MultiAPI;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;

import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition.WindowType;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

import uk.ac.imperial.lsds.streamsql.expressions.eint.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.Constant;

import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.GPUMicroAggregation;

import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.GPUSelection;

import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;

import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;

public class LRBQ4GPU {
	
	private MultiOperator multioperator;
	private MultiAPI api;
	
	public void setup (MultiAPI api) {
		
		this.api = api;
		Map<Integer, IWindowDefinition> windowDefs = new HashMap<Integer, IWindowDefinition>();
		
		/* Input stream is `vehicleId`, `speed`, `highway`, `direction`, `segment` */
		
		/*
		 * Query 4 is
		 * 
		 * Select segment, direction, highway
		 * From SegSpeedStr [Range 5 Minutes]
		 * Group By segment, direction, highway
		 * Having Avg(speed) < 40
		 *
		 */
		
		GPUSelection havingClause = new GPUSelection
		(
			new ComparisonPredicate<FloatType>(
				
				ComparisonPredicate.LESS_OP, 
				new ColumnReference<FloatType>(3), 
				new Constant<FloatType>(new FloatType(40f))
			)
		);
		
		IMicroOperatorCode code = new GPUMicroAggregation
		(
			GPUMicroAggregation.AggregationType.AVG, 
			1, 
			new int[] {2, 3, 4},
			new PrimitiveType [] {new IntegerType(0),  new FloatType(0f), new IntegerType(0)},
			havingClause
		);
		
		IMicroOperatorConnectable aggr = QueryBuilder.newMicroOperator(code, 3);
		Set<IMicroOperatorConnectable> ops = new HashSet<IMicroOperatorConnectable>();
		ops.add(aggr);
		
		windowDefs = new HashMap<Integer, IWindowDefinition>();
		windowDefs.put(12, new WindowDefinition(WindowType.RANGE_BASED, 300, 1));
		
		ISubQueryConnectable q4 = QueryBuilder.newSubQuery (ops, 5, windowDefs);
		
		Set<ISubQueryConnectable> subQueries = new HashSet<ISubQueryConnectable>();
		subQueries.add(q4);
		
		this.multioperator = MultiOperator.synthesizeFrom(subQueries, 100);
		this.multioperator.setUp();
	}
	
	public void process (MultiOpTuple tuple) 
	{
		this.multioperator.processData (tuple, this.api);
	}
}
