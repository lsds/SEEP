import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ReductionKernel;

public class TestReductionKernel {

	public static void main(String [] args) {
		
		WindowDefinition window = 
			new WindowDefinition (TestUtils.TYPE, TestUtils.RANGE, TestUtils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (TestUtils.OFFSETS, TestUtils._TUPLE_);
		
		ReductionKernel reductionCode = new ReductionKernel (
			AggregationType.SUM, 
			new FloatColumnReference(1),
			schema
		);
		reductionCode.setBatchSize(200);
		reductionCode.setInputSize(1024 * 1024);
		reductionCode.setup();
		
		System.out.println(String.format("[DBG] %s",reductionCode));
		
		MicroOperator uoperator = new MicroOperator (reductionCode, 1);
		
		/* Query */
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, new QueryConf(200, 1024));
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
