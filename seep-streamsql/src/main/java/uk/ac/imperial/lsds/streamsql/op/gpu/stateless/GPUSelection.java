package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class GPUSelection implements IStreamSQLOperator, IMicroOperatorCode {
	
	private IPredicate predicate;
	
	public GPUSelection(IPredicate predicate) {
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
	
	public IPredicate getPredicate() {
		return this.predicate;
	}
	
	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
	}
}
