import java.nio.ByteOrder;
import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.SelectionKernel;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestSelectionKernel {

	public static void main(String [] args) {
		
		String filename = args[0];
		
		WindowDefinition window = 
			new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		
		SelectionKernel selectionCode = new SelectionKernel (
			new IntComparisonPredicate (IntComparisonPredicate.LESS_OP, new IntColumnReference(1), new IntConstant(40)),
			schema,
			filename
		);
		/* System.out.println(String.format("[DBG] %s", selectionCode)); */
		
		IQueryBuffer buffer = new UnboundedQueryBuffer(Utils.BUNDLE);
		/* Populate input data */
		buffer.getByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
		while (buffer.getByteBuffer().hasRemaining())
			buffer.getByteBuffer().putInt(1);
		buffer.close();
		WindowBatch batch = new WindowBatch(1, buffer, window, schema);
		batch.setBatchPointers(0, buffer.capacity());
		
		selectionCode.processData(batch, null);
		
		byte [] output = selectionCode.getOutput();
		if (Arrays.equals(buffer.array(), output)) {
			System.out.println("OK");
		} else {
			System.out.println("Error");
		}
	}
}

