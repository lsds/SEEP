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
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.SimpleThetaJoinKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ThetaJoinKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestJoinSelectivity {

	public static void main(String [] args) {
		
		if (args.length != 12) {
			
			System.err.println("Invalid parameters:");
			
			System.err.println("\t- 1. Execution mode ['cpu','gpu','hybrid']");
			System.err.println("\t- 2. # CPU threads");
			System.err.println("\t- 3. # tuples in either stream for join task");
			System.err.println("\t- 4. 1st window type ['row','range']");
			System.err.println("\t- 5. 1st window range");
			System.err.println("\t- 6. 1st window slide");
			System.err.println("\t- 7. # attributes/tuple in 1st stream (excl. timestamp)");
			System.err.println("\t- 8. 2nd window type ['row','range']");
			System.err.println("\t- 9. 2nd window range");
			System.err.println("\t-10. 2nd window slide");
			System.err.println("\t-11. # attributes/tuple in 2nd stream (excl. timestamp)");
			System.err.println("\t-12. % selectivity [0-100]");
			
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
		
		WindowType firstWindowType = WindowType.fromString(args[3]);
		
		long firstWindowRange = Long.parseLong(args[4]);
		long firstWindowSlide = Long.parseLong(args[5]);
		
		WindowDefinition firstWindow = 
			new WindowDefinition (firstWindowType, firstWindowRange, firstWindowSlide);
		
		int firstNumberOfAttributesInSchema = Integer.parseInt(args[6]);
		
		int [] firstOffsets = new int[firstNumberOfAttributesInSchema + 1];
		firstOffsets[0] = 0;
		int firstByteSize = 8;
		for (int i = 1; i < firstNumberOfAttributesInSchema + 1; i++) {
			firstOffsets[i] = firstByteSize;
			firstByteSize += 4;
		}
		
		ITupleSchema firstSchema = new TupleSchema (firstOffsets, firstByteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		firstSchema.setType(0, 3);
		for (int i = 1; i < firstNumberOfAttributesInSchema + 1; i++) {
			firstSchema.setType(i, 1);
		}
		
		WindowType secondWindowType = WindowType.fromString(args[7]);
		
		long secondWindowRange = Long.parseLong(args[8]);
		long secondWindowSlide = Long.parseLong(args[9]);
		
		WindowDefinition secondWindow = 
			new WindowDefinition (secondWindowType, secondWindowRange, secondWindowSlide);
		
		int secondNumberOfAttributesInSchema = Integer.parseInt(args[10]);
		
		int [] secondOffsets = new int[secondNumberOfAttributesInSchema + 1];
		secondOffsets[0] = 0;
		int secondByteSize = 8;
		for (int i = 1; i < secondNumberOfAttributesInSchema + 1; i++) {
			secondOffsets[i] = secondByteSize;
			secondByteSize += 4;
		}
		
		ITupleSchema secondSchema = new TupleSchema (secondOffsets, secondByteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		secondSchema.setType(0, 3);
		for (int i = 1; i < secondNumberOfAttributesInSchema + 1; i++) {
			secondSchema.setType(i, 1);
		}
		
		int selectivity = Integer.parseInt(args[11]);

		IPredicate predicate =  new IntComparisonPredicate(
				IntComparisonPredicate.LESS_OP, 
				new IntColumnReference(1),
				new IntColumnReference(1));
		
		StringBuilder s = new StringBuilder();
		s.append("return (__bswap32(p1->tuple._1) < __bswap32(p2->tuple._1)) ? 1 : 0;");
		
		/* Calculate batch-related statistics */
		/* ... */
		
		Utils._CIRCULAR_BUFFER_ = 256 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 256 * 1024 * 1024;
		
		TheGPU.getInstance().init(1);
		
		IMicroOperatorCode cpuJoinCode = new ThetaJoin (predicate);
		System.out.println(String.format("[DBG] %s", cpuJoinCode));
		/*
		IMicroOperatorCode gpuJoinCode = new ThetaJoinKernel(predicate, firstSchema, secondSchema);
		
		((ThetaJoinKernel) gpuJoinCode).setBatchSize (batchSize);
		((ThetaJoinKernel) gpuJoinCode).setCustomFunctor (s.toString());
		((ThetaJoinKernel) gpuJoinCode).setup();
		*/
		IMicroOperatorCode gpuJoinCode = new SimpleThetaJoinKernel(predicate, firstSchema, secondSchema);
		
		((SimpleThetaJoinKernel) gpuJoinCode).setBatchSize (batchSize);
		((SimpleThetaJoinKernel) gpuJoinCode).setCustomFunctor (s.toString());
		((SimpleThetaJoinKernel) gpuJoinCode).setOutputSize (1024768);
		((SimpleThetaJoinKernel) gpuJoinCode).setup();
		
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuJoinCode, cpuJoinCode, 1);
		else
			uoperator = new MicroOperator (cpuJoinCode, gpuJoinCode, 1);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		long timestampReference = System.nanoTime();
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, firstSchema, firstWindow, queryConf, 
				secondSchema, secondWindow, timestampReference);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		
		operator.setup();
		
		TheCPU.getInstance().bind(0);

		/*
		 * Set up the stream
		 */
		int firstTuplesPerInsert  = 100;
		int secondTuplesPerInsert = 100;
		
		int firstTupleSize  =  firstSchema.getByteSizeOfTuple();
		int secondTupleSize = secondSchema.getByteSizeOfTuple();
		
		int firstBufferBundle  =  firstTupleSize *  firstTuplesPerInsert;
		int secondBufferBundle = secondTupleSize * secondTuplesPerInsert;
		
		byte [] firstData  = new byte [ firstBufferBundle];
		byte [] secondData = new byte [secondBufferBundle];
		
		ByteBuffer  firstBuffer = ByteBuffer.wrap( firstData);
		ByteBuffer secondBuffer = ByteBuffer.wrap(secondData);
		
		/* Fill the first buffer */
		int value = 0;
		long count = 0;
		while (firstBuffer.hasRemaining()) {
			firstBuffer.putLong(count++);
			firstBuffer.putInt(value);
			value = (value + 1) % 100;
			for (int i = 12; i < firstTupleSize; i += 4)
				firstBuffer.putInt(1);
		}
		/* Fill the second buffer */
		count = 0;
		while (secondBuffer.hasRemaining()) {
			secondBuffer.putLong(count++);
			secondBuffer.putInt(selectivity);
			for (int i = 12; i < secondTupleSize; i += 4)
				secondBuffer.putInt(1);
		}
		
		/* Populate time stamps */
		if (Utils.LATENCY_ON) {
			long ts = (System.nanoTime() - timestampReference) / 1000L;
			long packed = Utils.pack(ts, firstBuffer.getLong(0));
			firstBuffer.putLong(0, packed);
		}
		try {
			while (true) {
				operator.processData (firstData);
				operator.processDataSecond (secondData);
				if (Utils.LATENCY_ON)
					firstBuffer.putLong(0, Utils.pack((long) ((System.nanoTime() - timestampReference) / 1000L), 1));
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
