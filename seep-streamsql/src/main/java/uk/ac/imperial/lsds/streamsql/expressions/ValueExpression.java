package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.NumericConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

/*
 * Having different T types in the constructor arguments
 *   does not result in exception in the constructor,
 *   but rather in evalString method.
 */
public class ValueExpression<T extends Comparable<T>> implements IValueExpression<T> {
	private static final long serialVersionUID = 1L;

	private T _constant;
	private final TypeConversion<T> _wrapper;

	public ValueExpression(TypeConversion<T> wrapper, T constant) {
		_constant = constant;
		_wrapper = wrapper;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<T> newExpr) {

	}

	@Override
	public T eval(DataTuple tuple) {
		return _constant;
	}

	@Override
	public String evalString(DataTuple tuple) {
		final T value = eval(tuple);
		return _wrapper.toString(value);
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
		if (_wrapper instanceof NumericConversion) {
			final NumericConversion makis = (NumericConversion) _wrapper;
			// double temp = makis.toDouble((Number) _constant);
			final double val = ((Number) _constant).doubleValue();
			final double temp = makis.toDouble(new Double(val));
			_constant = (T) makis.fromDouble(1.0 / temp);
		}
	}

	@Override
	public boolean isNegative() {
		if (_wrapper instanceof NumericConversion) {
			final NumericConversion makis = (NumericConversion) _wrapper;
			final double temp = makis.toDouble(_constant);
			return (temp < 0);
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Constant ").append(_constant.toString());
		return sb.toString();
	}
}