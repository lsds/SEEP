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
import uk.ac.imperial.lsds.streamsql.expressions.Expression;

import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;

import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;


public class TwoQueriesRealTimeMeasurements {

	public static void main(String [] args) {
		
		if (args.length != 4) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- number of threads");
			System.err.println("\t- window batch size for 1st query");
			System.err.println("\t- window batch size for 2nd query");
			System.err.println("\t- number of attributes in schema (excl. timestamp)");
			System.exit(-1);
		}
		
		/*
		 * Set up configuration of system
		 */
		Utils.CPU = true;
		Utils.GPU = false;
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = Integer.parseInt(args[0]);
		
		QueryConf queryConf1 = new QueryConf(Integer.parseInt(args[1]), 1024);
		QueryConf queryConf2 = new QueryConf(Integer.parseInt(args[2]), 1024);

		int nattributes  = Integer.parseInt(args[3]);
		
		/*
		 * Q1: 		
		 */
		WindowType windowType = WindowType.ROW_BASED;
		long windowRange = 1024;
		long windowSlide = 1024;
		
		WindowDefinition w1 = new WindowDefinition (windowType, windowRange, windowSlide);
		
		int[] offsets1 = new int[nattributes + 1];
		offsets1[0] = 0;
		int byteSize = 8;
		for (int i = 1; i < nattributes + 1; i++) {
			offsets1[i] = byteSize;
			byteSize += 4;
		}
		ITupleSchema schema1 = new TupleSchema (offsets1, byteSize);
		
		Utils._CIRCULAR_BUFFER_ = 32 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 1024 * 1024;
		
		Expression [] expression1 = new Expression [nattributes + 1];
		expression1[0] = new LongColumnReference(0);
		for (int i = 0; i < nattributes; i++) {
			expression1[i+1] = new IntColumnReference((i % (nattributes)) + 1);
		}
		
		IMicroOperatorCode projCode1 = new Projection(expression1);
		
		MicroOperator operator1;
		operator1 = new MicroOperator (projCode1, projCode1, 1);
		Set<MicroOperator> operators1 = new HashSet<MicroOperator>();
		operators1.add(operator1);
		SubQuery query1 = new SubQuery (0, operators1, schema1, w1, queryConf1);
		
		WindowDefinition w2 = new WindowDefinition (windowType, windowRange, windowSlide);
		
		ITupleSchema schema2 = new TupleSchema (offsets1, byteSize);
		
		Expression [] expression2 = new Expression [nattributes + 1];
		expression2[0] = new LongColumnReference(0);
		for (int i = 0; i < nattributes; i++) {
			expression2[i+1] = new IntColumnReference((i % (nattributes)) + 1);
		}
		
		IMicroOperatorCode projCode2 = new Projection(expression2);
		
		MicroOperator operator2;
		operator2 = new MicroOperator (projCode2, projCode2, 1);
		Set<MicroOperator> operators2 = new HashSet<MicroOperator>();
		operators2.add(operator2);
		SubQuery query2 = new SubQuery (1, operators2, schema2, w2, queryConf2);

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
		int tuplesPerInsert = 32768;
		int actualByteSize = schema1.getByteSizeOfTuple();
		int bufferBundle = actualByteSize * tuplesPerInsert;
		byte [] data = new byte [bufferBundle];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		/* Fill the buffer */
		while (b.hasRemaining()) {
			b.putLong(0);
			b.putFloat(1);
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
