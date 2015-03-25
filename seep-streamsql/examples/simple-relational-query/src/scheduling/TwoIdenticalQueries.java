package scheduling;
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
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatDivision;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatMultiplication;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateful.MicroAggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.AProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;

public class TwoIdenticalQueries {

	public static void main(String [] args) {
		
		if (args.length != 5) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- mode ('cpu', 'gpu', 'hybrid')");
			System.err.println("\t- number of threads");
			System.err.println("\t- numbers of windows in window batch for first query");
			System.err.println("\t- numbers of windows in window batch for second query");
			System.err.println("\t- number of attributes in tuple schema (excl. timestamp)");
			System.exit(-1);
		}
		
		/*
		 * Set up configuration of system
		 */
		Utils.CPU = false;
		Utils.GPU = false;
		if (args[0].toLowerCase().contains("cpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.CPU = true;
		if (args[0].toLowerCase().contains("gpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.GPU = true;
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = Integer.parseInt(args[1]);
		QueryConf queryConf1 = new QueryConf(Integer.parseInt(args[2]), 1024);
		QueryConf queryConf2 = new QueryConf(Integer.parseInt(args[3]), 1024);

		int numberOfAttributesInSchema  = Integer.parseInt(args[4]);
		
		TheGPU.getInstance().init(2);
		
		/*
		 * Q1		
		 */
		WindowType windowType = WindowType.ROW_BASED;
		long windowRange      = 1024;
		long windowSlide      = 1024;
		
		WindowDefinition window1 = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		int[] offsets1 = new int[numberOfAttributesInSchema + 1];
		// first attribute is timestamp
		offsets1[0] = 0;

		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets1[i] = byteSize;
			byteSize += 4;
		}
		
		ITupleSchema schema1 = new TupleSchema (offsets1, byteSize);
		
		long ppb = window1.panesPerSlide() * (queryConf1.BATCH - 1) + window1.numberOfPanes();
		long tpb = ppb * window1.getPaneSize();
		int inputSize = (int) tpb * schema1.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024; // Integer.parseInt(args[2]) * 128 * 32 * 1024; /* 64 tasks in queue */
		Utils._UNBOUNDED_BUFFER_ = inputSize;
		
		int batchOffset = (int) ((queryConf1.BATCH) * window1.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		int nestingDepth = 128;
		
		Expression [] expression1 = new Expression [numberOfAttributesInSchema + 1];
		// always include the timestamp
		expression1[0] = new LongColumnReference(0);
		
		for (int i = 0; i < numberOfAttributesInSchema; i++) {
			expression1[i+1] = new IntColumnReference((i % (numberOfAttributesInSchema)) + 1);
		}
		
		FloatExpression ex1_1 = new FloatColumnReference(1);
		for (int i = 0; i < nestingDepth; i++) {
			ex1_1 = new FloatDivision(new FloatMultiplication(new FloatConstant(3), ex1_1), new FloatConstant(2));
		}
		
		expression1[1] = ex1_1;
		
		IMicroOperatorCode projCode1 = new Projection(
			expression1
		);
		
		IMicroOperatorCode gpuProjectionCode1 = new AProjectionKernel(expression1, schema1, "/home/akolious/seep/seep-streamsql/examples/simple-relational-query/src/kernels/Projection.cl");
		((AProjectionKernel) gpuProjectionCode1).setInputSize(inputSize);
		((AProjectionKernel) gpuProjectionCode1).setup();
		
		MicroOperator operator1;
		if (Utils.GPU && ! Utils.HYBRID)
			operator1 = new MicroOperator (gpuProjectionCode1, projCode1, 1);
		else
			operator1 = new MicroOperator (projCode1, gpuProjectionCode1, 1);
		
//		= new MicroOperator (projCode1, 1);
		Set<MicroOperator> operators1 = new HashSet<MicroOperator>();
		operators1.add(operator1);
		SubQuery query1 = new SubQuery (0, operators1, schema1, window1, queryConf1);
		
		WindowDefinition window2 = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
//		int[] offsets2 = new int[3];
//		offsets2[0] = 0;
//		offsets2[0] = 8;
//		offsets2[0] = 12;
		
		ITupleSchema schema2 = new TupleSchema (offsets1, 32);
		
		Expression [] expression2 = new Expression [numberOfAttributesInSchema + 1];
		// always include the timestamp
		expression2[0] = new LongColumnReference(0);
		
		for (int i = 0; i < numberOfAttributesInSchema; i++) {
			expression2[i+1] = new IntColumnReference((i % (numberOfAttributesInSchema)) + 1);
		}
		
		FloatExpression ex2_1 = new FloatColumnReference(1);
		for (int i = 0; i < nestingDepth; i++) {
			ex2_1 = new FloatDivision(new FloatMultiplication(new FloatConstant(3), ex1_1), new FloatConstant(2));
		}
		
		expression2[1] = ex2_1;
		
		IMicroOperatorCode projCode2 = new Projection(
			expression2
		);
		
		IMicroOperatorCode gpuProjectionCode2 = new AProjectionKernel(expression2, schema2, "/home/akolious/seep/seep-streamsql/examples/simple-relational-query/src/kernels/Projection.cl");
		((AProjectionKernel) gpuProjectionCode2).setInputSize(inputSize);
		((AProjectionKernel) gpuProjectionCode2).setup();
		
		MicroOperator operator2;
		if (Utils.GPU && ! Utils.HYBRID)
			operator2 = new MicroOperator (gpuProjectionCode2, projCode2, 1);
		else
			operator2 = new MicroOperator (projCode2, gpuProjectionCode2, 1);
		// = new MicroOperator (projCode2, 2);
		Set<MicroOperator> operators2 = new HashSet<MicroOperator>();
		operators2.add(operator2);
		SubQuery query2 = new SubQuery (1, operators2, schema2, window2, queryConf2);

		query1.connectTo(10000, query2);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		queries.add(query1);
		queries.add(query2);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		System.out.println("1st schema " + schema1.getByteSizeOfTuple() + " bytes");
		System.out.println("2nd schema " + schema2.getByteSizeOfTuple() + " bytes");
		
		/*
		 * Set up the stream
		 */
		TheGPU.getInstance().bind(0);

		int tuplesPerInsert = 32768;
		int actualByteSize = schema1.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * tuplesPerInsert; // yields 1MB for byteSize = 32 
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data); // .order(ByteOrder.LITTLE_ENDIAN);
		
		// fill the buffer
		Random r = new Random();
		while (b.hasRemaining()) {
			b.putLong(0);
			// b.putFloat(r.nextFloat());
			b.putFloat(1);
			for (int i = 12; i < actualByteSize; i += 4)
				b.putInt(1);
		}
		
		/* Populate timestamps */
		long nextBatchStartPointer = 0L;
		long count = 1L;
		while (nextBatchStartPointer < tuplesPerInsert) {
			int normalisedIndex = (int) (nextBatchStartPointer % tuplesPerInsert) * actualByteSize;
			b.putLong(normalisedIndex, System.nanoTime());
			// System.out.println("Set batch timestamp at " + normalisedIndex + " to be " + b.getLong(normalisedIndex));
			nextBatchStartPointer += batchOffset;
		}
		
		try {
			while (true) {
				// for (int i = 0; i < 32768; i++)
				//	b.putLong(i * 32, 0L);
				operator.processData (data);
				count ++;
				while (nextBatchStartPointer < tuplesPerInsert * count) {
					int normalisedIndex = (int) (nextBatchStartPointer % tuplesPerInsert) * actualByteSize;
					b.putLong(normalisedIndex, System.nanoTime());
					// System.out.println("Set batch timestamp at " + normalisedIndex + " to be " + b.getLong(normalisedIndex));
					nextBatchStartPointer += batchOffset;
				}
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
