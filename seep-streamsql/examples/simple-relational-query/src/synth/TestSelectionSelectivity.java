package synth;
import java.nio.ByteBuffer;
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
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestSelectionSelectivity {

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
		Utils.BATCH = Integer.parseInt(args[2]);
		
		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString(args[3]);
		long windowRange      = Long.parseLong(args[4]);
		long windowSlide      = Long.parseLong(args[5]);
		int numberOfAttributesInSchema  = Integer.parseInt(args[6]);
		int selectivity                 = Integer.parseInt(args[7]);
		
		WindowDefinition window = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		
		int[] offsets = new int[numberOfAttributesInSchema + 1];
		// first attribute is timestamp
		offsets[0] = 0;

		int o = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = o;
			o += 4;
		}
		
		int byteSize = 1;
		while (o > byteSize)
			byteSize *= 2;
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		IPredicate predicate =  new IntComparisonPredicate(
						IntComparisonPredicate.LESS_OP, 
						new IntColumnReference(1),
						new IntConstant(selectivity));
				
		IMicroOperatorCode selectionCode = new Selection(predicate);
		System.out.println(String.format("[DBG] %s", selectionCode));
		
		/*
		 * Build and set up the query
		 */
		MicroOperator uoperator = new MicroOperator (selectionCode, 1);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();

		/*
		 * Set up the stream
		 */
		// yields 1MB for byteSize = 32 
		int bufferBundle = byteSize * 32768;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		// fill the buffer
		int value = 0;
		while (b.hasRemaining()) {
			b.putLong(1);
			b.putInt(value);
			value = (value + 1) % 100; 
			for (int i = 12; i < byteSize; i += 4)
				b.putInt(1);
		}
		
		try {
			while (true) 
				operator.processData (data);
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
