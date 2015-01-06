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
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;

public class TestProjectionAttributes {

	public static void main(String [] args) {
		
		if (args.length != 5) {
			System.err.println("Incorrect number of parameters, we need:");
			System.err.println("\t- window type ('row', 'range')");
			System.err.println("\t- window size ");
			System.err.println("\t- window slide");
			System.err.println("\t- number of attributes in tuple schema (excl. timestamp)");
			System.err.println("\t- number of projected attributes (excl. timestamp)");
			System.exit(-1);
		}
		
		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString(args[0]);
		long windowRange      = Long.parseLong(args[1]);
		long windowSlide      = Long.parseLong(args[2]);
		int numberOfAttributesInSchema  = Integer.parseInt(args[3]);
		int numberOfProjectedAttributes = Integer.parseInt(args[4]);
		
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
		
		Expression [] expression = new Expression [numberOfProjectedAttributes + 1];
		// always include the timestamp
		expression[0] = new LongColumnReference(0);
		
		for (int i = 0; i < numberOfProjectedAttributes; i++) {
			expression[i+1] = new IntColumnReference((i % (numberOfAttributesInSchema)) + 1);
		}
		
		IMicroOperatorCode projectionCode = new Projection (expression);
		System.out.println(String.format("[DBG] %s", projectionCode));
		
		/*
		 * Build and set up the query
		 */
		MicroOperator uoperator = new MicroOperator (projectionCode, 1);
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
		while (b.hasRemaining()) {
			b.putLong(1);
			for (int i = 0; i < numberOfAttributesInSchema; i++)
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
