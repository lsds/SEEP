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
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateful.MicroAggregationKernel;
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
		
		Utils.THREADS = Integer.parseInt(args[1]);
		QueryConf queryConf1 = new QueryConf(Integer.parseInt(args[2]), 1024);
		QueryConf queryConf2 = new QueryConf(Integer.parseInt(args[3]), 1024);

		int numberOfAttributesInSchema  = Integer.parseInt(args[4]);
		
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
		
		IMicroOperatorCode projCode1 = new Projection(
			new Expression [] {
				new LongColumnReference(0),
				new IntColumnReference(1),
				new IntColumnReference(2),
				new IntColumnReference(3),
				new IntColumnReference(4),
				new IntColumnReference(5),
				new IntColumnReference(6)
			}
		);
		
		MicroOperator operator1 = new MicroOperator (projCode1, 1);
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
		
		IMicroOperatorCode projCode2 = new Projection(
			new Expression [] {
				new LongColumnReference(0),
				new IntColumnReference(1),
				new IntColumnReference(2),
				new IntColumnReference(3),
				new IntColumnReference(4),
				new IntColumnReference(5),
				new IntColumnReference(6)
			}
		);
		
		MicroOperator operator2 = new MicroOperator (projCode2, 2);
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
		// yields 1MB for byteSize = 32 
		int actualByteSize = schema1.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * 32768;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		// fill the buffer
		Random r = new Random();
		int firstGroupByAtt  = 0;
		int secondGroupByAtt = 0;
		while (b.hasRemaining()) {
			b.putLong(1);
			b.putFloat(r.nextFloat());
			// attributes, which will be used for grouping
			b.putInt(firstGroupByAtt++);
			firstGroupByAtt = firstGroupByAtt % 10;
			b.putInt(secondGroupByAtt++);
			secondGroupByAtt = secondGroupByAtt % 10;
			
			for (int i = 20; i < actualByteSize; i += 4)
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
