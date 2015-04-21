package synth;
import java.nio.ByteBuffer;
import java.util.HashSet;
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
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatMultiplication;

import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;

import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;

import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.AProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;

public class TestProjectionAttributes {

	public static void main(String [] args) {
		
		if (args.length != 8) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- mode ('cpu', 'gpu', 'hybrid')");
			System.err.println("\t- number of CPU threads");
			System.err.println("\t- numbers of windows in window batch");
			System.err.println("\t- window type ('row', 'range')");
			System.err.println("\t- window size ");
			System.err.println("\t- window slide");
			System.err.println("\t- number of attributes in tuple schema (excl. timestamp)");
			System.err.println("\t- number of projected attributes (excl. timestamp)");
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
		QueryConf queryConf = new QueryConf(Integer.parseInt(args[2]), 1024);

		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString(args[3]);
		long windowRange      = Long.parseLong(args[4]);
		long windowSlide      = Long.parseLong(args[5]);
		int numberOfAttributesInSchema  = Integer.parseInt(args[6]);
		int numberOfProjectedAttributes = Integer.parseInt(args[7]);
		
		WindowDefinition window = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		int[] offsets = new int[numberOfAttributesInSchema + 1];
		// first attribute is timestamp
		offsets[0] = 0;

		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		Expression [] expression = new Expression [numberOfProjectedAttributes + 1];
		// always include the timestamp
		expression[0] = new LongColumnReference(0);
		
		for (int i = 0; i < numberOfProjectedAttributes; i++) {
			expression[i+1] = new IntColumnReference((i % (numberOfAttributesInSchema)) + 1);
		}
		
		int nestingDepth = 8;
		
		FloatExpression ex1 = new FloatColumnReference(1);
		for (int i = 0; i < nestingDepth; i++) {
			ex1 = new FloatDivision(new FloatMultiplication(new FloatConstant(3), ex1), new FloatConstant(2));
		}
//		
//		IntExpression ex2 = new IntColumnReference(2);
//		for (int i = 0; i < nestingDepth; i++) {
//			ex2 = new IntAddition(new IntMultiplication(new IntConstant(3), ex2), new IntConstant(1));
//		}
//		
//		IntExpression ex3 = new IntColumnReference(3);
//		for (int i = 0; i < nestingDepth; i++) {
//			ex3 = new IntAddition(new IntMultiplication(new IntConstant(3), ex3), new IntConstant(1));
//		}
//		
//		IntExpression ex4 = new IntColumnReference(4);
//		for (int i = 0; i < nestingDepth; i++) {
//			ex4 = new IntAddition(new IntMultiplication(new IntConstant(3), ex4), new IntConstant(1));
//		}
		
		expression[1] = ex1;
//		expression[2] = ex2;
//		expression[3] = ex3;
//		expression[4] = ex4;
		
		long ppb = window.panesPerSlide() * (queryConf.BATCH - 1) + window.numberOfPanes();
		long tpb = ppb * window.getPaneSize();
		int inputSize = (int) tpb * schema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		
		int batchOffset = (int) ((queryConf.BATCH) * window.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		TheGPU.getInstance().init(1);
		
		IMicroOperatorCode projectionCode = new Projection (expression);
		System.out.println(String.format("[DBG] %s", projectionCode));
		IMicroOperatorCode gpuProjectionCode = new AProjectionKernel(expression, schema);
		
		((AProjectionKernel) gpuProjectionCode).setInputSize(inputSize);
		((AProjectionKernel) gpuProjectionCode).setup();
		
		/*
		 * Build and set up the query
		 */
		
		
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024; // Integer.parseInt(args[2]) * 64 * 32 * 1024; /* 64 tasks in queue */
		Utils._UNBOUNDED_BUFFER_ = inputSize;
		
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuProjectionCode, projectionCode, 1);
		else
			uoperator = new MicroOperator (projectionCode, gpuProjectionCode, 1);
		
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, queryConf);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		
		operator.setup();
		
		TheCPU.getInstance().bind(0);

		/*
		 * Set up the stream
		 */
		
		int tuplesPerInsert = 32768;
		int actualByteSize = schema.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * tuplesPerInsert; // yields 1MB for byteSize = 32 
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data); // .order(ByteOrder.LITTLE_ENDIAN);
		
		// fill the buffer
		// Random r = new Random();
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
		
//		// yields 1MB for byteSize = 32 
//		int actualByteSize = schema.getByteSizeOfTuple();
//		int bufferBundle = actualByteSize * 32768;
//		byte [] data = new byte [bufferBundle];
//		ByteBuffer b = ByteBuffer.wrap(data);
//		// b.order(ByteOrder.LITTLE_ENDIAN);
//		
//		// fill the buffer
//		while (b.hasRemaining()) {
//			b.putLong(System.nanoTime());
//			b.putFloat(1);
//			for (int i = 12; i < actualByteSize; i += 4)
//				b.putInt(1);
//		}
//		
//		// System.out.println("[DBG] First timestamp is " + b.getLong(0));
//		
//		try {
//			while (true) {
//				operator.processData (data);
//				b.putLong(0, System.nanoTime());
////				System.out.println("[DBG] Second timestamp is " + b.getLong(0));
////				operator.processData (data);
////				b.putLong(0, System.nanoTime());
////				System.out.println("[DBG] Third timestamp is " + b.getLong(0));
////				operator.processData (data);
////				b.putLong(0, System.nanoTime());
////				System.out.println("[DBG] Fourth timestamp is " + b.getLong(0));
////				operator.processData (data);
////				break;
//				/* Update timestamps */
//				// for (int i = 0; i < 32768; i++)
//				//	b.putLong(i * actualByteSize, System.nanoTime());
//				// b.putLong(0, System.nanoTime());
//			}
//		} catch (Exception e) { 
//			e.printStackTrace(); 
//			System.exit(1);
//		}
	}
}
