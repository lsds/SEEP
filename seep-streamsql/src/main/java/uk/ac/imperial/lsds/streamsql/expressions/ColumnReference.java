package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class ColumnReference<T extends Comparable<T>> implements IValueExpression<T> {
	private static final long serialVersionUID = 1L;

	private String _column;
	private TypeConversion<T> _wrapper;

	public ColumnReference(TypeConversion<T> wrapper, String column) {
		_column = column;
		_wrapper = wrapper;
	}


	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<T> newExpr) {
		// nothing
	}

	@Override
	public T eval(DataTuple tuple) {
		final String value = tuple.getValue(_column).toString();
		return _wrapper.fromString(value);
	}

	@Override
	public String evalString(DataTuple tuple) {
		return tuple.getValue(_column).toString();
	}

	public String getColumn() {
		return _column;
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		return new ArrayList<IValueExpression>();
	}

	@Override
	public TypeConversion getType() {
		return _wrapper;
	}

	@Override
	public void inverseNumber() {
		// nothing

	}

	@Override
	public boolean isNegative() {
		return false;
	}

	public void setColumn(String column) {
		_column = column;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (_column != null)
			sb.append("\"").append(_column).append("\"");
		return sb.toString();
	}

}
