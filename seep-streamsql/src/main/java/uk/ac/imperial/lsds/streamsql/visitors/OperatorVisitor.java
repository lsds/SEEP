package uk.ac.imperial.lsds.streamsql.visitors;

import net.sf.jsqlparser.statement.select.Distinct;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;

public interface OperatorVisitor {

	public void visit(Projection projection);
	
	public void visit(Selection selection);
}
