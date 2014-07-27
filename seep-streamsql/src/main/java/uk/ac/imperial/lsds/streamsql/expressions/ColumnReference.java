package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class ColumnReference<T extends PrimitiveType> implements IValueExpression<T> {
	private static final long serialVersionUID = 1L;

	private int _column = -1;

	public ColumnReference(int column) {
		_column = column;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public T eval(MultiOpTuple tuple) {
		return (T) tuple.values[_column];
	}

	public int getColumn() {
		return _column;
	}

	@Override
	public IValueExpression<T>[] getInnerExpressions() {
		return new IValueExpression[0];
	}


	public void setColumn(int column) {
		_column = column;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (_column != -1)
			sb.append("\"").append(_column).append("\"");
		return sb.toString();
	}

}
