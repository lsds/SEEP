package scheduling;
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
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.op.gpu.TheGPU;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ReductionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ASelectionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TwoQuerySelectAggregate {

	public static void main(String [] args) {
		
		if (args.length != 7) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- mode ('cpu', 'gpu', 'hybrid')");
			System.err.println("\t- number of threads");
			System.err.println("\t- numbers of windows in window batch for first query");
			System.err.println("\t- numbers of windows in window batch for second query");
			System.err.println("\t- number of attributes in tuple schema (excl. timestamp)");
			System.err.println("\t- kernel filename 1");
			System.err.println("\t- kernel filename 2");
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
		
		String filename1 = args[5];
		
		String filename2 = args[6];
		
		TheGPU.getInstance().init(2);
		
		/*
		 * 32-byte tuples (1 timestamp, 6 attributes)
		 * Q1: select * from S [row 1024 slide 1024] where "1" <= 100
		 */ 
		
		WindowType windowType1 = WindowType.ROW_BASED;
		long windowRange1      = 1024;
		long windowSlide1      = 1024;
		AggregationType aggregationType = AggregationType.MIN;
		
		WindowDefinition window1 = 
			new WindowDefinition (windowType1, windowRange1, windowSlide1);
		
		int[] offsets1 = new int[numberOfAttributesInSchema + 1];
		// first attribute is timestamp
		offsets1[0] = 0;

		int byteSize1 = 8;
		for (int i = 1; i < numberOfAttributesInSchema + 1; i++) {
			offsets1[i] = byteSize1;
			byteSize1+= 4;
		}
		
		ITupleSchema schema1 = new TupleSchema (offsets1, byteSize1);
		
		float selectivity = 100;
//		IPredicate predicate =  new FloatComparisonPredicate(
//				IntComparisonPredicate.LESS_OP, 
//				new FloatColumnReference(1),
//				new FloatConstant(selectivity));
		
		int numComparisons = 32;
		
		IPredicate [] predicates = new IPredicate[numComparisons];
		
		for (int i = 0; i < numComparisons; i++) {
			predicates[i] = new FloatComparisonPredicate(
					FloatComparisonPredicate.NONLESS_OP, 
					new FloatColumnReference(1),
					new FloatConstant(0));
		}
		
		IPredicate predicate =  new ANDPredicate(predicates);
		
		IMicroOperatorCode selectionCode = new Selection(predicate);
		System.out.println(String.format("[DBG] %s", selectionCode));
		IMicroOperatorCode gpuSelectionCode = new ASelectionKernel(predicate, schema1, filename1);
				
		MicroOperator operator1;
		if (Utils.GPU && ! Utils.HYBRID)
			operator1 = new MicroOperator (gpuSelectionCode, selectionCode, 1);
		else
			operator1 = new MicroOperator (selectionCode, gpuSelectionCode, 1);
		
		Set<MicroOperator> operators1 = new HashSet<MicroOperator>();
		operators1.add(operator1);
		SubQuery query1 = new SubQuery (0, operators1, schema1, window1, queryConf1);


		/*
		 * Q2: select sum("1") from R [rows 1024 slide 1024]
		 */
		WindowType windowType2 = WindowType.ROW_BASED;
		long windowRange2      = 1024;
		long windowSlide2      = 1024;
		
		WindowDefinition window2 = 
			new WindowDefinition (windowType2, windowRange2, windowSlide2);
		
		int [] offsets2 = offsets1;
		ITupleSchema schema2 = new TupleSchema (offsets2, schema1.getByteSizeOfTuple());
		
		IMicroOperatorCode aggCode = new Selection(predicate);
		IMicroOperatorCode gpuAggCode = new ASelectionKernel(predicate, schema1, filename1);
		
//		long ppb = window2.panesPerSlide() * (queryConf2.BATCH - 1) + window2.numberOfPanes();
//		long tpb = ppb * window2.getPaneSize();
//		int inputSize = (int) tpb * schema2.getByteSizeOfTuple();
//		System.out.println(String.format("[DBG] %d bytes input", inputSize));
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		
//		IMicroOperatorCode aggCode = new MicroAggregation(
//				window1,
//				aggregationType,
//				new FloatColumnReference(1)
//				);
//		
//		System.out.println(String.format("[DBG] %s", aggCode));
//		ReductionKernel gpuAggCode = new ReductionKernel (
//				aggregationType,
//				new FloatColumnReference(1),
//				schema2
//				);
//		gpuAggCode.setSource (filename2);
//		gpuAggCode.setBatchSize(queryConf2.BATCH);
		/* More... */
//		long ppb = window2.panesPerSlide() * (queryConf2.BATCH - 1) + window2.numberOfPanes();
//		long tpb = ppb * window2.getPaneSize();
//		int inputSize = (int) tpb * schema2.getByteSizeOfTuple();
//		System.out.println(String.format("[DBG] %d bytes", inputSize));
//		gpuAggCode.setInputSize (inputSize);
//		gpuAggCode.setup();
		
//		ReductionKernel gpuAggCode = new ReductionKernel (
//				aggregationType,
//				new FloatColumnReference(1),
//				schema2
//				);
		
		/// TheGPU.getInstance().init(1);
		
//		gpuAggCode.setSource (filename2);
//		gpuAggCode.setBatchSize(queryConf2.BATCH);
//		/* Configure... */
//		gpuAggCode.setInputSize (inputSize);
//		gpuAggCode.setup();

		MicroOperator operator2;
		if (Utils.GPU && ! Utils.HYBRID)
			operator2 = new MicroOperator (gpuAggCode, aggCode, 1);
		else
			operator2 = new MicroOperator (aggCode, gpuAggCode, 1);
		
		Set<MicroOperator> operators2 = new HashSet<MicroOperator>();
		operators2.add(operator2);
		SubQuery query2 = new SubQuery (1, operators2, schema2, window2, queryConf2);

		query1.connectTo(10000, query2);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		queries.add(query1);
		queries.add(query2);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();

		/*
		 * Set up the stream
		 */
		// yields 1MB for byteSize = 32 
		int actualByteSize = schema1.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * 32768;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		// fill the buffer
		float value = 0;
		while (b.hasRemaining()) {
			b.putLong(1);
			b.putFloat(value);
			value = (value + 1) % 100; 
			for (int i = 12; i < actualByteSize; i += 4)
				b.putInt(1);
		}
		
		try {
			while (true) {
				operator.processData (data);
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
