package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.expressions.Addition;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.Division;
import uk.ac.imperial.lsds.streamsql.expressions.Multiplication;
import uk.ac.imperial.lsds.streamsql.expressions.Subtraction;

public interface ValueExpressionVisitor {

	public void visit(Addition add);

	public void visit(ColumnReference cr);

	public void visit(Division dvsn);

	public void visit(Multiplication mult);

	public void visit(Subtraction sub);

	public void visit(Constant vs);
}
