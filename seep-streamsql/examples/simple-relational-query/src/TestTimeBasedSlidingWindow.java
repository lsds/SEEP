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
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ASelectionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestTimeBasedSlidingWindow {

	public static void main(String [] args) {
		
		if (args.length != 4) {
			
			System.err.println("Invalid parameters:");
			
			System.err.println("\t- 1. # window batch size");
			System.err.println("\t- 2. Window range");
			System.err.println("\t- 3. Window slide");
			System.err.println("\t- 4. # attributes/tuple (excl. timestamp)");
			
			System.exit(-1);
		}
		
		Utils.CPU = true;
		Utils.GPU = false;
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = 15;
		
		int batchSize = Integer.parseInt(args[0]);
		
		QueryConf queryConf = new QueryConf (batchSize, 1024);
		
		WindowType windowType = WindowType.fromString("range");
		
		long windowRange = Long.parseLong(args[1]);
		long windowSlide = Long.parseLong(args[2]);
		
		WindowDefinition window = 
				new WindowDefinition (windowType, windowRange, windowSlide);
		
		int numberOfAttributesInSchema  = Integer.parseInt(args[3]);
			
		int [] offsets = new int[numberOfAttributesInSchema + 1];
		offsets[0] = 0;
		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		schema.setType(0, 3);
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			schema.setType(i, 1);
		}
		
		IPredicate predicate = 
				new IntComparisonPredicate
				(
					IntComparisonPredicate.GREATER_OP, 
					new IntColumnReference(1),
					new IntConstant(0)
				);
		
		int inputSize = queryConf.BATCH;
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		
		IMicroOperatorCode cpuSelectionCode = new Selection (predicate);
		System.out.println(String.format("[DBG] %s", cpuSelectionCode));
		
		MicroOperator uoperator;
		uoperator = new MicroOperator (cpuSelectionCode, null, 1);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Utils._CIRCULAR_BUFFER_ = 32 * 1024 * 1024;
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
		
		int tuplesPerInsert = 4096;
		int tupleSize = schema.getByteSizeOfTuple();
		int bufferBundle = tupleSize * tuplesPerInsert;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		/* Fill the buffer */
		long timestamp = 0;
		while (b.hasRemaining()) {
			b.putLong(timestamp);
			for (int i = 8; i < tupleSize; i += 4)
				b.putInt(1);
		}
		
		try {
			while (true) {
				operator.processData (data);
				/* Update timestamp */
				timestamp += 1;
				b.clear();
				while (b.hasRemaining()) {
					b.putLong(timestamp);
					b.position(b.position() + 24);
					// for (int i = 8; i < tupleSize; i += 4)
					//	b.putInt(1);
				}
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
