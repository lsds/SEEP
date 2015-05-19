package synth;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TheCPU;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatDivision;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatMultiplication;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntAddition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntMultiplication;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.AggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.MultiMicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestMultiAggregationGroupBy {
	
	public static void main(String [] args) {
		
		if (args.length != 9) {
			
			System.err.println("Invalid parameters:");
			
			System.err.println("\t- 1. Execution mode ['cpu','gpu','hybrid']");
			System.err.println("\t- 2. # CPU threads");
			System.err.println("\t- 3. # windows/batch");
			System.err.println("\t- 4. Window type ['row','range']");
			System.err.println("\t- 5. Window range");
			System.err.println("\t- 6. Window slide");
			System.err.println("\t- 7. # attributes/tuple (excl. timestamp)");
			System.err.println("\t- 8. # aggregations");
			System.err.println("\t- 9. # groups");
			
			System.exit(-1);
		}
		
		Utils.CPU = false;
		Utils.GPU = false;
		if (args[0].toLowerCase().contains("cpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.CPU = true;
		if (args[0].toLowerCase().contains("gpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.GPU = true;
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = Integer.parseInt(args[1]);
		
		int batchSize = Integer.parseInt(args[2]);
		QueryConf queryConf = new QueryConf (batchSize, 1024);
		
		WindowType windowType = WindowType.fromString(args[3]);
		
		long windowRange = Long.parseLong(args[4]);
		long windowSlide = Long.parseLong(args[5]);
		
		WindowDefinition window = 
				new WindowDefinition (windowType, windowRange, windowSlide);
		
		int numberOfAttributesInSchema = Integer.parseInt(args[6]);
		
		int[] offsets = new int[numberOfAttributesInSchema + 1];
		offsets[0] = 0;
		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		
		int numberOfAggregates = Integer.parseInt(args[7]);
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		schema.setType(0, 3);
		for (int i = 1; i <= numberOfAggregates; i++)
			schema.setType(1, 2);
		for (int i = numberOfAggregates + 1; i < numberOfAttributesInSchema + 1; i++) {
			schema.setType(i, 1);
		}
		
		int ngroups = Integer.parseInt(args[8]);
		
		AggregationType [] aggregationType = new AggregationType [numberOfAggregates];
		for (int i = 0; i < numberOfAggregates; i++)
			aggregationType[i] = AggregationType.fromString("avg");
		
		/* Calculate batch-related statistics */
		long ppb = window.panesPerSlide() * (queryConf.BATCH - 1) + window.numberOfPanes();
		long tpb = ppb * window.getPaneSize();
		int inputSize = (int) tpb * schema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		int batchOffset = (int) ((queryConf.BATCH) * window.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		TheGPU.getInstance().init(1);
				
		Expression [] groupBy = new Expression [] {
			  new IntColumnReference(1 + numberOfAggregates)
			/* , new IntColumnReference(1 + numberOfAggregates + 1)
			, new IntColumnReference(1 + numberOfAggregates + 2) */
		};
		
		FloatColumnReference [] aggregationAttribute = new FloatColumnReference[numberOfAggregates];
		for (int i = 0; i < numberOfAggregates; i++)
			aggregationAttribute[i] = new FloatColumnReference(i + 1);
		
		
		IPredicate [] predicates = new IPredicate[2];
		predicates[0] = new FloatComparisonPredicate
			(
				FloatComparisonPredicate.GREATER_OP, 
				new FloatColumnReference(2),
				new FloatConstant(0)
			);
		predicates[1] = new FloatComparisonPredicate
			(
				FloatComparisonPredicate.GREATER_OP,
				new FloatColumnReference(3),
				new FloatConstant(0)
			);
		IPredicate predicate = new ANDPredicate(predicates);
		
		Expression [] aggregationExpression = new Expression [numberOfAggregates];
		for (int i = 0; i < numberOfAggregates; i++) {
			aggregationExpression[i] = new FloatDivision(new FloatMultiplication(new FloatConstant(3), new FloatColumnReference(i + 2)), new FloatConstant(2));
		}
		
		IMicroOperatorCode cpuAggCode = new MultiMicroAggregation
			(
				window,
				aggregationType,
				aggregationAttribute,
				aggregationExpression,
				groupBy
				/* , new Selection(predicate) */ , null, null
			);
		
		System.out.println(String.format("[DBG] %s", cpuAggCode));
		
		IMicroOperatorCode gpuAggCode = null;
		/* new AggregationKernel
			(
				aggregationType,
				new FloatColumnReference(1), 
				groupBy, 
				null,
				null, 
				schema
			);
		
		((AggregationKernel) gpuAggCode).setInputSize(inputSize);
		((AggregationKernel) gpuAggCode).setBatchSize(queryConf.BATCH);
		((AggregationKernel) gpuAggCode).setWindowSize((int) window.getSize());
		((AggregationKernel) gpuAggCode).setup();
		*/
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuAggCode, cpuAggCode, 1);
		else 
			uoperator = new MicroOperator (cpuAggCode, gpuAggCode, 1);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = inputSize;
		
		long timestampReference = System.nanoTime();
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, queryConf, timestampReference);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		
		operator.setup();
		
		TheCPU.getInstance().bind(0);
		
		/*
		 * Set up the stream
		 */
		int tuplesPerInsert = 32768;
		int tupleSize = schema.getByteSizeOfTuple();
		int bufferBundle = tupleSize * tuplesPerInsert;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		/* Fill the buffer */
		Random r = new Random();
		int g = 1;
		while (b.hasRemaining()) {
			b.putLong(1); // time stamp
			for (int i = 0; i < numberOfAggregates; i++)
				b.putFloat(r.nextFloat()); // the aggregate
			b.putInt(g++); // group by attribute
			g = g % ngroups;
			// System.out.println("g = " + g);
			if (g == 0)
				g = 1;
			for (int i = 8 + 4 * numberOfAggregates + 4; i < tupleSize; i += 4)
				b.putInt(1);
		}
		
		if (Utils.LATENCY_ON) {
			/* Populate time stamps */
			long ts = (System.nanoTime() - timestampReference) / 1000L;
			long packed = Utils.pack(ts, b.getLong(0));
			b.putLong(0, packed);
		}
		try {
			while (true) {
				operator.processData (data);
				if (Utils.LATENCY_ON)
					b.putLong(0, Utils.pack((long) ((System.nanoTime() - timestampReference) / 1000L), 1));
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
