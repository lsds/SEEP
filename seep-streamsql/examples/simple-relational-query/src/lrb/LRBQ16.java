package lrb;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntDivision;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntMultiplication;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntSubtraction;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class LRBQ16 {

	private MultiOperator	mo;

	public void setup() {

		// INPUT STREAM timestamp, vehicleID, speed, highway, direction,
		// position
		int[] offsets = new int[] { 0, 8, 12, 16, 20, 24, 28 };
		int byteSize = 32;
		ITupleSchema inputSchema = new TupleSchema(offsets, byteSize);

		/*
		 * Query 1
		 * 
		 * Select vehicleId, speed, xPos/5280 as segNo, dir, hwy 
		 * From PosSpeedStr
		 */
		Expression[] projExpressions = new Expression[] {
				new LongColumnReference(0),
				new IntColumnReference(1),
				new FloatColumnReference(2),
				new IntDivision(new IntColumnReference(5),
						new IntConstant(5280)), new IntColumnReference(4),
				new IntColumnReference(3) };

		IMicroOperatorCode q1ProjCode = new Projection(projExpressions);
		MicroOperator q1Proj = new MicroOperator(q1ProjCode, 1);

		Set<MicroOperator> q1MicroOps = new HashSet<>();
		q1MicroOps.add(q1Proj);

		SubQuery sq1 = new SubQuery(10, q1MicroOps, inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024), new QueryConf(200, 1024));

		/*
		 * Query 2
		 * 
		 * Select Distinct L.vehicleId, L.segNo, L.dir, L.hwy 
		 * From SegSpeedStr [Range 30 Seconds] As A,
		 *      SegSpeedStr [Partition by vehicleId Rows 1] As L
		 * Where A.vehicleId = L.vehicleId
		 */
		MicroOperator q2m = new MicroOperator(new LRBQ2RStreamMicroOpCode(), 2);
		Set<MicroOperator> q2MicroOps = new HashSet<>();
		q2MicroOps.add(q2m);
		SubQuery sq2 = new SubQuery(20, q2MicroOps, inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024), new QueryConf(200, 1024));

		/*
		 * Query 3
		 * 
		 * Select Istream(*)
		 * From ActiveVehicleStream
		 */
		MicroOperator q3m = new MicroOperator(new IStreamMicroOpCode(), 3);
		Set<MicroOperator> q3MicroOps = new HashSet<>();
		q3MicroOps.add(q3m);
		SubQuery sq3 = new SubQuery(20, q3MicroOps, inputSchema,
				new WindowDefinition(WindowType.RANGE_BASED, 1024, 1023), new QueryConf(200, 1024));

		/*
		 * Query 4
		 * 
		 * Select segNo, dir, hwy 
		 * From SegSpeedStr [Range 5 Minutes] 
		 * Group By segNo, dir, hwy 
		 * Having Avg(speed) < 40
		 */
		// OUTPUT STREAM timestamp, segNo, dir, hwy, AVG(speed)

		WindowDefinition window4 = new WindowDefinition(WindowType.ROW_BASED, 1024, 1024);

		Selection having = new Selection(new FloatComparisonPredicate(
				FloatComparisonPredicate.LESS_OP, new FloatColumnReference(3),
				new FloatConstant(40f)));

		IMicroOperatorCode q4AggCode = new MicroAggregation(
				window4,
				AggregationType.AVG, new FloatColumnReference(1),
				new Expression[] { new IntColumnReference(2),
						new IntColumnReference(3), new IntColumnReference(4) },
				having);

		MicroOperator q4Agg = new MicroOperator(q4AggCode, 4);

		IMicroOperatorCode q4ProjCode = new Projection(new Expression[] {
				new FloatColumnReference(1),
				new FloatColumnReference(2), new FloatColumnReference(3) });

		MicroOperator q4Proj = new MicroOperator(q4ProjCode, 5);

		q4Agg.connectTo(1, q4Proj);

		Set<MicroOperator> q4MicroOps = new HashSet<>();
		q4MicroOps.add(q4Proj);
		q4MicroOps.add(q4Agg);

		SubQuery sq4 = new SubQuery(40, q4MicroOps, inputSchema,
				window4, new QueryConf(200, 1024));
		
		/*
		 * Query 5
		 * 
		 * Select segNo, dir, hwy, count(vehicleId) 
		 * From ActiveVehicleSegRel
		 * Group By segNo, dir, hwy 
		 */
		WindowDefinition window5 = new WindowDefinition(WindowType.ROW_BASED, 1024, 1024);

		IMicroOperatorCode q5AggCode = new MicroAggregation(
				window5,
				AggregationType.COUNT, new FloatColumnReference(1),
				new Expression[] { new IntColumnReference(2),
						new IntColumnReference(3), new IntColumnReference(4) });

		MicroOperator q5Agg = new MicroOperator(q5AggCode, 6);

		IMicroOperatorCode q5ProjCode = new Projection(new Expression[] {
				new FloatColumnReference(1),
				new FloatColumnReference(2), new FloatColumnReference(3) });

		MicroOperator q5Proj = new MicroOperator(q5ProjCode, 7);

		q5Agg.connectTo(1, q5Proj);

		Set<MicroOperator> q5MicroOps = new HashSet<>();
		q5MicroOps.add(q5Proj);
		q5MicroOps.add(q5Agg);

		SubQuery sq5 = new SubQuery(50, q5MicroOps, inputSchema,
				window5, new QueryConf(200, 1024));

		/*
		 * Query 6
		 * 
		 * Select RStream(E.vehicleId, basetoll * (V.numVehicles - 150) * (V.numVehicles - 150) as toll)
		 * From VehicleSegEntryStr [Now]  as E,
		 *      CongestedSegRel as C,
		 *      SegVolRel as V
		 * Where E.segNo = C.segNo And C.segNo = V.segNo
		 * And E.dir = C.dir And C.dir = V.dir
		 * And E.hwy = C.hwy And C.hwy = V.hwy
		 */
		
		IPredicate pred6a = new ANDPredicate(
				new IntComparisonPredicate(new IntColumnReference(1), new IntColumnReference(1)),
				new FloatComparisonPredicate(new FloatColumnReference(2), new FloatColumnReference(2)),				
				new IntComparisonPredicate(new IntColumnReference(3), new IntColumnReference(3))				
				);
		
		MicroOperator q6am = new MicroOperator(new ThetaJoin(pred6a), 8);
		Set<MicroOperator> q6aMicroOps = new HashSet<>();
		q6aMicroOps.add(q6am);
		SubQuery sq6a = new SubQuery(60, q6aMicroOps, inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024), 
				new QueryConf(200, 1024),
				inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024)
				); 

		IPredicate pred6b = new ANDPredicate(
				new IntComparisonPredicate(new IntColumnReference(1), new IntColumnReference(1)),
				new FloatComparisonPredicate(new FloatColumnReference(2), new FloatColumnReference(2)),				
				new IntComparisonPredicate(new IntColumnReference(3), new IntColumnReference(3))				
				);
		
		MicroOperator q6bm = new MicroOperator(new ThetaJoin(pred6b), 9);
		
		IMicroOperatorCode q6ProjCode = new Projection(new Expression[] {
				new IntColumnReference(1),
				new IntMultiplication(
						new IntConstant(42),
						new IntConstant(2),
						new IntSubtraction(new IntColumnReference(2), new IntConstant(150))
						)
				});

		MicroOperator q6Proj = new MicroOperator(q6ProjCode, 11);

		q6bm.connectTo(1, q6Proj);
		
		Set<MicroOperator> q6bMicroOps = new HashSet<>();
		q6bMicroOps.add(q6bm);
		q6bMicroOps.add(q6Proj);
		SubQuery sq6b = new SubQuery(61, q6bMicroOps, inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024), 
				new QueryConf(200, 1024),
				inputSchema,
				new WindowDefinition(WindowType.ROW_BASED, 1024, 1024)
				); 

		
		sq1.connectTo(101, sq4);
		sq1.connectTo(102, sq2);
		sq2.connectTo(103, sq3);
		sq2.connectTo(104, sq5);

		sq3.connectTo(105, sq6a);
		sq4.connectTo(106, sq6a);
		sq5.connectTo(107, sq6b);
		sq6a.connectTo(108, sq6b);
		sq5.connectTo(109, sq6b);
		
		Set<SubQuery> subQueries = new HashSet<>();
		subQueries.add(sq1);
		subQueries.add(sq2);
		subQueries.add(sq3);
		subQueries.add(sq4);
		subQueries.add(sq5);
		subQueries.add(sq6a);
		subQueries.add(sq6b);

		this.mo = new MultiOperator(subQueries, 1001);
		this.mo.setup();

	}

	public void process(byte[] values) {
		this.mo.processData(values);
	}

}
