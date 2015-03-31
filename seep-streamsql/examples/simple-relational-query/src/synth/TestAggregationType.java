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
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ReductionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;

public class TestAggregationType {

	public static void main(String [] args) {
		
		if (args.length != 9) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- mode ('cpu', 'gpu', 'hybrid')");
			System.err.println("\t- number of CPU threads");
			System.err.println("\t- numbers of windows in window batch");
			System.err.println("\t- window type ('row', 'range')");
			System.err.println("\t- window size ");
			System.err.println("\t- window slide");
			System.err.println("\t- number of attributes in tuple schema (excl. timestamp)");
			System.err.println("\t- aggregation type ('avg', 'sum', 'count', 'max', 'min')");
			System.err.println("\t- GPU kernel filename");
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
		
		int batchSize = Integer.parseInt(args[2]);
		
		Utils.THREADS = Integer.parseInt(args[1]);
		QueryConf queryConf = new QueryConf(batchSize, 1024);
		
		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString(args[3]);
		long windowRange      = Long.parseLong(args[4]);
		long windowSlide      = Long.parseLong(args[5]);
		int numberOfAttributesInSchema  = Integer.parseInt(args[6]);
		AggregationType aggregationType = AggregationType.fromString(args[7]);
		
		String filename = args[8];
		
		WindowDefinition window = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		int batchOffset = (int) ((batchSize) * window.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		int[] offsets = new int[numberOfAttributesInSchema + 1];
		// first attribute is timestamp
		offsets[0] = 0;

		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		long ppb = window.panesPerSlide() * (queryConf.BATCH - 1) + window.numberOfPanes();
		long tpb = ppb * window.getPaneSize();
		int inputSize = (int) tpb * schema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
				
		IMicroOperatorCode aggCode = new MicroAggregation(
				window,
				aggregationType,
				new FloatColumnReference(1)
				);
		
		System.out.println(String.format("[DBG] %s", aggCode));
		ReductionKernel gpuAggCode = new ReductionKernel (
				aggregationType,
				new FloatColumnReference(1),
				schema
				);
		
		TheGPU.getInstance().init(1);
		
		gpuAggCode.setSource (filename);
		gpuAggCode.setBatchSize(queryConf.BATCH);
		/* Configure... */
		gpuAggCode.setInputSize (inputSize);
		gpuAggCode.setup();
		
		/*
		 * Build and set up the query
		 */
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuAggCode, aggCode, 1);
		else
			uoperator = new MicroOperator (aggCode, gpuAggCode, 1);
		
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, queryConf);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		TheGPU.getInstance().bind(0);

		/*
		 * Set up the stream
		 */
		// yields 1MB for byteSize = 32 
		int tuplesPerInsert = 32768 / 1024;
		int actualByteSize = schema.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * tuplesPerInsert; // yields 1MB for byteSize = 32 
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		// fill the buffer
		Random r = new Random();
		while (b.hasRemaining()) {
			b.putLong(0);
			b.putFloat(r.nextFloat());
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
