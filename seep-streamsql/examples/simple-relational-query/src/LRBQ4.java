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
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntAddition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;

public class LRBQ4 {

	private MultiOperator	mo;

	public void setup() {

		// INPUT STREAM timestamp, vehicleID, speed, highway, direction,
		// position
		int[] offsets = new int[] { 0, 8, 12, 16, 20, 24, 28 };
		int byteSize = 32;
		ITupleSchema inputSchema = new TupleSchema(offsets, byteSize);

		/*
		 * Query 4
		 * 
		 * Select segNo, dir, hwy From SegSpeedStr [Range 5 Minutes] Group By
		 * segNo, dir, hwy Having Avg(speed) < 40
		 */
		// OUTPUT STREAM timestamp, segNo, dir, hwy, AVG(speed)

		Selection having = new Selection(new FloatComparisonPredicate(
				FloatComparisonPredicate.LESS_OP, new FloatColumnReference(3),
				new FloatConstant(40f)));

		IMicroOperatorCode q2AggCode = new MicroAggregation(
				AggregationType.AVG, new FloatColumnReference(1),
				new Expression[] { new IntColumnReference(2),
						new IntColumnReference(3), new IntColumnReference(4) },
				having);

		MicroOperator q2Agg = new MicroOperator(q2AggCode, 3);

		IMicroOperatorCode q2ProjCode = new Projection(new Expression[] {
				new FloatColumnReference(1),
				new IntAddition(new IntExpression[] {
						new IntColumnReference(5), new IntConstant(2) }),
				new FloatColumnReference(2), new FloatColumnReference(3) });

		MicroOperator q2Proj = new MicroOperator(q2ProjCode, 5);

		q2Agg.connectTo(1, q2Proj);

		Set<MicroOperator> q2MicroOps = new HashSet<>();
		q2MicroOps.add(q2Proj);
		q2MicroOps.add(q2Agg);

		// SubQuery sq1 = new SubQuery(10, q2MicroOps, inputSchema, new
		// WindowDefinition(WindowType.RANGE_BASED, 300, 1));
		SubQuery sq4 = new SubQuery(40, q2MicroOps, inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, Utils.RANGE, Utils.SLIDE));

		Set<SubQuery> subQueries = new HashSet<>();
		subQueries.add(sq4);

		this.mo = new MultiOperator(subQueries, 101);
		this.mo.setup();

	}

	public void processData(byte[] values) {
		this.mo.processData(values);
	}

}
