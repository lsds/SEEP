package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.expressions.Addition;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.DateDiff;
import uk.ac.imperial.lsds.streamsql.expressions.DateSum;
import uk.ac.imperial.lsds.streamsql.expressions.Division;
import uk.ac.imperial.lsds.streamsql.expressions.IntegerYearFromDate;
import uk.ac.imperial.lsds.streamsql.expressions.Multiplication;
import uk.ac.imperial.lsds.streamsql.expressions.StringConcatenate;
import uk.ac.imperial.lsds.streamsql.expressions.Subtraction;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;

public interface ValueExpressionVisitor {

	public void visit(Addition add);

	public void visit(ColumnReference cr);

	public void visit(DateSum ds);

	public void visit(DateDiff dd);	

	public void visit(Division dvsn);

	public void visit(IntegerYearFromDate iyfd);

	public void visit(Multiplication mult);

	public void visit(StringConcatenate sc);

	public void visit(Subtraction sub);

	public void visit(ValueExpression vs);
}
