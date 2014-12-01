import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ProjectionKernel;

public class TestProjectionKernel {

	public static void main(String [] args) {
		
		WindowDefinition window = 
			new WindowDefinition (Utils.TYPE, Utils.RANGE, Utils.SLIDE);
		
		ITupleSchema schema = new TupleSchema (Utils.OFFSETS, Utils._TUPLE_);
		
		IMicroOperatorCode projectionCode = new ProjectionKernel (
			new Expression [] {
				new IntColumnReference(1),
				new IntColumnReference(2),
				new IntColumnReference(3),
				new IntColumnReference(4),
				new IntColumnReference(5),
				new IntColumnReference(6)
			}
		);
		
		System.out.println(String.format("[DBG] %s", projectionCode));
	}
}

