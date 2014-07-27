package uk.ac.imperial.lsds.streamsql.op.stateless;

import java.util.Arrays;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Selection implements IStreamSQLOperator, IMicroOperatorCode {

	private IPredicate predicate;

	public Selection(IPredicate predicate) {
		this.predicate = predicate;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
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
				
				int count = 0;
				while (windowStart <= windowEnd) {
					/*
					 * Check whether predicate is satisfied for tuple 
					 */
					MultiOpTuple tuple = batch.get(windowStart);
					if (this.predicate.satisfied(tuple)) 
						windowResult[count++] = tuple;
					
					windowStart++;
				}
				// make sure to shrink the array to the actual number of selected tuples
				api.outputWindowResult(Arrays.copyOf(windowResult, count));
			}
		}
	}
}
