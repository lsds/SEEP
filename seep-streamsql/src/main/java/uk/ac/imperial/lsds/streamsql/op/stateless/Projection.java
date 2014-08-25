package uk.ac.imperial.lsds.streamsql.op.stateless;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Projection implements IStreamSQLOperator, IMicroOperatorCode {

	/*
	 * Expressions for the extended projection
	 */
	private IValueExpression<PrimitiveType>[] expressions;


	public Projection(IValueExpression<PrimitiveType>[] expressions) {
		this.expressions = expressions;
	}

	@SuppressWarnings("unchecked")
	public Projection(IValueExpression<PrimitiveType> expression) {
		this.expressions = (IValueExpression<PrimitiveType>[]) new IValueExpression[] {expression};
	}

	private MultiOpTuple copyProject(MultiOpTuple data) {
		MultiOpTuple t = new MultiOpTuple();
		t.values = new Object[expressions.length];

		/*
		 * Add all the content as defined by the projection expressions
		 */
		for (int i = 0; i < expressions.length; i++) 
			t.values[i] = expressions[i].eval(data);
		
		t.timestamp = data.timestamp;
		t.instrumentation_ts = data.instrumentation_ts;
		
		return t;
	}
		
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (IValueExpression<PrimitiveType> att : expressions)
			sb.append(att.toString() + " ");
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);		
	}

	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		
		assert(windowBatches.keySet().size() == 1);
		
		IWindowBatch batch = windowBatches.values().iterator().next();
		
		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			// empty window?
			if (windowStart == -1) {
				api.outputWindowResult(new MultiOpTuple[0]);
			}
			else {
				
				MultiOpTuple[] windowResult = new MultiOpTuple[windowEnd-windowStart+1];
				
				for (int i = 0; i < windowEnd-windowStart+1; i++) 
					windowResult[i] = copyProject(batch.get(windowStart + i));
				
				api.outputWindowResult(windowResult);
			}
		}
	}
}
