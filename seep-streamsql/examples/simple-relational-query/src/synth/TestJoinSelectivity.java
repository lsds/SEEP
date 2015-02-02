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
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestJoinSelectivity {

	public static void main(String [] args) {
		
		if (args.length != 12) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- mode ('cpu', 'gpu', 'hybrid')");
			System.err.println("\t- number of CPU threads");
			System.err.println("\t- number of tuples in either stream for join");
			System.err.println("\t- first window type ('row', 'range')");
			System.err.println("\t- first window size ");
			System.err.println("\t- first window slide");
			System.err.println("\t- number of attributes in first tuple schema (excl. timestamp)");
			System.err.println("\t- second window type ('row', 'range')");
			System.err.println("\t- second window size ");
			System.err.println("\t- second window slide");
			System.err.println("\t- number of attributes in second tuple schema (excl. timestamp)");
			System.err.println("\t- selectivity in percent (0 <= x <= 100)");
			System.exit(-1);
		}
		
		/*
		 * Set up configuration of system
		 */
		if (args[0].toLowerCase().contains("cpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.CPU = true;
		if (args[0].toLowerCase().contains("gpu") || args[0].toLowerCase().contains("hybrid"))
			Utils.GPU = true;
		
		Utils.THREADS = Integer.parseInt(args[1]);
		QueryConf queryConf = new QueryConf(Integer.parseInt(args[2]), 1024);
		
		/*
		 * Set up configuration of query
		 */
		WindowType firstWindowType = WindowType.fromString(args[3]);
		long firstWindowRange      = Long.parseLong(args[4]);
		long firstWindowSlide      = Long.parseLong(args[5]);
		int firstNumberOfAttributesInSchema  = Integer.parseInt(args[6]);
		
		WindowType secondWindowType = WindowType.fromString(args[7]);
		long secondWindowRange      = Long.parseLong(args[8]);
		long secondWindowSlide      = Long.parseLong(args[9]);
		int secondNumberOfAttributesInSchema  = Integer.parseInt(args[10]);

		int selectivity             = Integer.parseInt(args[11]);
		
		WindowDefinition firstWindow = 
			new WindowDefinition (firstWindowType, firstWindowRange, firstWindowSlide);
		
		WindowDefinition secondWindow = 
				new WindowDefinition (secondWindowType, secondWindowRange, secondWindowSlide);
		
		int[] firstOffsets = new int[firstNumberOfAttributesInSchema + 1];
		// first attribute is timestamp
		firstOffsets[0] = 0;

		int firstByteSize = 8;
		for (int i = 1; i < firstNumberOfAttributesInSchema + 1; i++) {
			firstOffsets[i] = firstByteSize;
			firstByteSize += 4;
		}
		
		ITupleSchema firstSchema = new TupleSchema (firstOffsets, firstByteSize);
		
		int[] secondOffsets = new int[secondNumberOfAttributesInSchema + 1];
		// first attribute is timestamp
		secondOffsets[0] = 0;

		int secondByteSize = 8;
		for (int i = 1; i < secondNumberOfAttributesInSchema + 1; i++) {
			secondOffsets[i] = secondByteSize;
			secondByteSize += 4;
		}
		
		ITupleSchema secondSchema = new TupleSchema (secondOffsets, secondByteSize);

		IPredicate predicate =  new IntComparisonPredicate(
				IntComparisonPredicate.LESS_OP, 
				new IntColumnReference(1),
				new IntColumnReference(1));
		
		IMicroOperatorCode joinCode = new ThetaJoin(predicate);
		System.out.println(String.format("[DBG] %s", joinCode));
		//TODO: use code for GPU computation
		IMicroOperatorCode gpuJoinCode = new ThetaJoin(predicate);
		
		/*
		 * Build and set up the query
		 */
		MicroOperator uoperator = new MicroOperator (joinCode, gpuJoinCode, 1);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, firstSchema, firstWindow, queryConf, secondSchema, secondWindow);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();

		/*
		 * Set up the stream
		 */
		int firstActualByteSize  = firstSchema.getByteSizeOfTuple();
		int secondActualByteSize = secondSchema.getByteSizeOfTuple();
		// 32768 yields 1MB for byteSize = 32 
		// 16384 yields 1MB for byteSize = 16 
		int firstBufferBundle = firstActualByteSize * 200;
		int secondBufferBundle = secondActualByteSize * 200;
		byte [] firstData = new byte [firstBufferBundle];
		byte [] secondData = new byte [secondBufferBundle];
		ByteBuffer firstB = ByteBuffer.wrap(firstData);
		ByteBuffer secondB = ByteBuffer.wrap(secondData);
		
		int value = 0;
		// fill the first buffer
		while (firstB.hasRemaining()) {
			firstB.putLong(1);
			firstB.putInt(value);
			value = (value + 1) % 100; 
			for (int i = 12; i < firstActualByteSize; i += 4)
				firstB.putInt(1);
		}
		// fill the second buffer
		while (secondB.hasRemaining()) {
			secondB.putLong(1);
			secondB.putInt(selectivity);
			for (int i = 12; i < secondActualByteSize; i += 4)
				secondB.putInt(1);
		}
		
		try {
			while (true) {
				operator.processData (firstData);
				operator.processDataSecond (secondData);
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
