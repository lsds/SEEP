package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.operator.Distinct;
import uk.ac.imperial.lsds.streamsql.operator.Projection;
import uk.ac.imperial.lsds.streamsql.operator.Selection;

public interface OperatorVisitor {

//	public void visit(AggregateOperator aggregation);
//
//	public void visit(ChainOperator chain);

	public void visit(Distinct distinct);

	public void visit(Projection projection);

	public void visit(Selection selection);

}
