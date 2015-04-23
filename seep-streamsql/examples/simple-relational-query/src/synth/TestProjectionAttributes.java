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
	
	private static long pack (long left, long right) {
		return (left << 32) | right;
	}
	
	public static int unpack (int idx, long value) {
        if (idx == 0) { /* left */
            return (int) (value >> 32);
        } else
        if (idx == 1) { /* right value */
            return (int) value;
        } else {
            return -1;
        }
    }
	
	public static void main(String [] args) {
		
		if (args.length != 9) {
			
			System.err.println("Invalid parameters:");
			
			System.err.println("\t- 1. Execution mode ['cpu','gpu','hybrid']");
			System.err.println("\t- 2. # CPU threads");
			System.err.println("\t- 3. # windows/batch");
			System.err.println("\t- 4. Window type ['row','range']");
			System.err.println("\t- 5. Window range");
			System.err.println("\t- 6. Window slide");
			System.err.println("\t- 7. # attributes/tuple     (excl. timestamp)");
			System.err.println("\t- 8. # projected attributes (excl. timestamp)");
			System.err.println("\t- 9. Depth of arithmetic expression");
			
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
		
		int numberOfAttributesInSchema  = Integer.parseInt(args[6]);
		int numberOfProjectedAttributes = Integer.parseInt(args[7]);
		
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
		
		Expression [] expression = new Expression [numberOfProjectedAttributes + 1];
		/* Always project the time stamp */
		expression[0] = new LongColumnReference(0);
		
		for (int i = 0; i < numberOfProjectedAttributes; i++) {
			expression[i+1] = new IntColumnReference((i % (numberOfAttributesInSchema)) + 1);
		}
		/* Introduce 0 or more arithmetic expressions */
		int nestingDepth = Integer.parseInt(args[8]);
		FloatExpression fexpr = new FloatColumnReference(1);
		for (int i = 0; i < nestingDepth; i++) {
			fexpr = new FloatDivision (new FloatMultiplication(new FloatConstant(3), fexpr), new FloatConstant(2));
		}
		expression[1] = fexpr;
		
		/* Calculate batch-related statistics */
		long ppb = window.panesPerSlide() * (queryConf.BATCH - 1) + window.numberOfPanes();
		long tpb = ppb * window.getPaneSize();
		int inputSize = (int) tpb * schema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		int batchOffset = (int) ((queryConf.BATCH) * window.getSlide());
		System.out.println("[DBG] offset is " + batchOffset);
		
		TheGPU.getInstance().init(1);
		
		IMicroOperatorCode cpuProjectionCode = new Projection (expression);
		System.out.println(String.format("[DBG] %s", cpuProjectionCode));
		
		IMicroOperatorCode gpuProjectionCode = new AProjectionKernel (expression, schema);
		
		((AProjectionKernel) gpuProjectionCode).setInputSize(inputSize);
		((AProjectionKernel) gpuProjectionCode).setDepth(nestingDepth);
		((AProjectionKernel) gpuProjectionCode).setup();
		
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuProjectionCode, cpuProjectionCode, 1);
		else
			uoperator = new MicroOperator (cpuProjectionCode, gpuProjectionCode, 1);
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
		long ts = (System.nanoTime() - timestampReference) / 1000L;
		long packed = pack(ts, b.getLong(0));
		b.putLong(0, packed);
		try {
			while (true) {	
				operator.processData (data);
				b.putLong(0, pack((long) ((System.nanoTime() - timestampReference) / 1000L), 1));
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
