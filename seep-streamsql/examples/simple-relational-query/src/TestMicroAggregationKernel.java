import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateful.MicroAggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;

public class TestMicroAggregationKernel {

	public static void main(String [] args) {
		
		String filename = args[0];
		
		int batchSize = 100;
		
		WindowDefinition window = 
			new WindowDefinition (TestUtils.TYPE, TestUtils.RANGE, TestUtils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (TestUtils.OFFSETS, TestUtils._TUPLE_);
		
		Selection having = new Selection 
		(
			new FloatComparisonPredicate 
			(
				FloatComparisonPredicate.LESS_OP, 
				new FloatColumnReference(3), 
				new FloatConstant(40f)
			)
		);
		
		MicroAggregationKernel aggregation = new MicroAggregationKernel
		(
			AggregationType.AVG, 
			new FloatColumnReference(1),
			new Expression[] {
				new IntColumnReference(2),
				new IntColumnReference(3),
				new IntColumnReference(4)
			},
			having,
			schema
		);
		
		/* aggregation.setSource(filename); */
		aggregation.setWindowDefinition(window);
		aggregation.setBatchSize(batchSize);
		aggregation.setup();
		
		/* System.out.println(String.format("[DBG] %s", aggregation)); */
		
		IQueryBuffer buffer = new UnboundedQueryBuffer(0, Utils.BUNDLE, false);
		/* Populate input data */
		buffer.getByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
		while (buffer.getByteBuffer().hasRemaining())
			buffer.getByteBuffer().putInt(1);
		buffer.close();
		WindowBatch batch = new WindowBatch(batchSize, 0, 0, buffer, window, schema);
		batch.setBatchPointers(0, buffer.capacity());
		
		aggregation.processData(batch, null);
	}
}

