import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ReductionKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;

public class TestReductionKernel {

	public static void main(String [] args) {
		
		String filename = args[0];
		
		WindowDefinition window = 
			new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		
		ReductionKernel reductionCode = new ReductionKernel (
			AggregationType.AVG, 
			new FloatColumnReference(1),
			schema
		);
		reductionCode.setSource(filename);
		reductionCode.setBatchSize(Utils.BATCH);
		reductionCode.setup();
		
		System.out.println(String.format("[DBG] %s",reductionCode));
		
		MicroOperator uoperator = new MicroOperator (reductionCode, 1);
		
		/* Query */
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window);
		queries.add(query);
		
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		byte [] data = new byte [Utils.BUNDLE];
		ByteBuffer b = ByteBuffer.wrap(data);
		b.order(ByteOrder.LITTLE_ENDIAN);
		Random r = new Random();
		while (b.hasRemaining()) {
			b.putLong(1);
			int value = r.nextInt(100);
			if (value == 0)
				value = 100;
			b.putFloat(value);
			for (int i = 12; i < schema.getByteSizeOfTuple(); i += 4)
				b.putInt(1);
		}
		try {
			while (true) {
				operator.processData (data);
				/* Thread.sleep(100L); */
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			System.exit(1);
		}
	}
}
