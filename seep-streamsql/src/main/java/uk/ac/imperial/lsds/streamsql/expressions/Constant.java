package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class Constant<T extends PrimitiveType> implements IValueExpression<T> {

	private T _constant;

	public Constant(T constant) {
		_constant = constant;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public T eval(MultiOpTuple tuple) {
		return _constant;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Constant ").append(_constant.toString());
		return sb.toString();
	}

	@Override
	public IValueExpression<T>[] getInnerExpressions() {
		return null;
	}
}