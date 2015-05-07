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
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.DummyKernel;

public class TestDummyKernel {

	public static void main(String [] args) {
		
		if (args.length != 5) {
			
			System.err.println("Invalid parameters:");
			
			System.err.println("\t- 1. # windows/batch");
			System.err.println("\t- 2. Window type ['row','range']");
			System.err.println("\t- 3. Window range");
			System.err.println("\t- 4. Window slide");
			System.err.println("\t- 5. # attributes/tuple (excl. timestamp)");
			
			System.exit(-1);
		}
		
		Utils.CPU = false;
		Utils.GPU = true;
		
		Utils.THREADS = 1;
		
		int batchSize = Integer.parseInt(args[0]);
		QueryConf queryConf = new QueryConf (batchSize, 1024);
		
		WindowType windowType = WindowType.fromString(args[1]);
		
		long windowRange = Long.parseLong(args[2]);
		long windowSlide = Long.parseLong(args[3]);
		
		WindowDefinition window = 
				new WindowDefinition (windowType, windowRange, windowSlide);
		
		int numberOfAttributesInSchema  = Integer.parseInt(args[4]);
		
		int[] offsets = new int[numberOfAttributesInSchema + 1];
		offsets[0] = 0;
		int byteSize = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		schema.setType(0, 3);
		schema.setType(1, 2);
		for (int i = 2; i < numberOfAttributesInSchema + 1; i++) {
			schema.setType(i, 1);
		}
		
		/* Calculate batch-related statistics */
		long ppb = window.panesPerSlide() * (queryConf.BATCH - 1) + window.numberOfPanes();
		long tpb = ppb * window.getPaneSize();
		int inputSize = (int) tpb * schema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		int batchOffset = (int) ((queryConf.BATCH) * window.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		TheGPU.getInstance().init(1);
		
		IMicroOperatorCode dummyKernel = new DummyKernel (schema, inputSize);
		
		((DummyKernel) dummyKernel).setup();
		
		MicroOperator uoperator;
		uoperator = new MicroOperator (dummyKernel, dummyKernel, 1);
		
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Utils._CIRCULAR_BUFFER_ = 64 * 1024 * 1024;
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
		while (b.hasRemaining()) {
			b.putLong (1);
			b.putFloat(1); /* Should this be a random number? */
			for (int i = 12; i < tupleSize; i += 4)
				b.putInt(1);
		}
		
		/* Populate time stamps */
		if (Utils.LATENCY_ON) {
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
