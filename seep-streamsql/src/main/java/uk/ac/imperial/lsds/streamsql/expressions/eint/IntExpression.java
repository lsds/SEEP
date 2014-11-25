package uk.ac.imperial.lsds.streamsql.expressions.eint;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public interface IntExpression extends Expression {
	
	public void accept(ValueExpressionVisitor vev);

	public int eval(IQueryBuffer buffer, ITupleSchema schema, int offset);
	
}