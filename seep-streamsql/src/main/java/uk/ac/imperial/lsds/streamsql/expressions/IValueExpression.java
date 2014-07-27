package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public interface IValueExpression<T extends PrimitiveType> {
	
	public void accept(ValueExpressionVisitor vev);

	public T eval(MultiOpTuple tuple);

	public IValueExpression<T>[] getInnerExpressions();

}