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

import uk.ac.imperial.lsds.streamsql.op.stateless.Noop;


public class TwoNoopQueries {

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
		
		IMicroOperatorCode noop1 = new Noop ();
		
		MicroOperator operator1 = new MicroOperator (noop1, 1);
		Set<MicroOperator> operators1 = new HashSet<MicroOperator>();
		operators1.add(operator1);
		SubQuery query1 = new SubQuery (0, operators1, schema1, window1, queryConf1);
		
		WindowDefinition window2 = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		ITupleSchema schema2 = new TupleSchema (offsets1, 32);
		
		IMicroOperatorCode noop2 = new Noop ();
		
		MicroOperator operator2 = new MicroOperator (noop2, 2);
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
		
		while (b.hasRemaining())
			b.putInt(1);
		
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
