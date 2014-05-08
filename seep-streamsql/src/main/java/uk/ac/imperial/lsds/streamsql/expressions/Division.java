package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.DoubleConversion;
import uk.ac.imperial.lsds.streamsql.conversion.NumericConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

/*
 * This class implements Division between any Number type (Integer, Double, Long, etc.).
 * It convert all the value to double, and then return the final result by automatic casting
 *   (i.e. (int)1.0 )
 *
 * Double can store integers exatly in binary representation,
 *   so we won't lose the precision on our integer operations.
 *
 * Having different T types in the constructor arguments
 *   does not result in exception in the constructor,
 *   but rather in eval method.
 */
public class Division implements IValueExpression<Double> {

	private static final long serialVersionUID = 1L;

	private final List<IValueExpression> _veList = new ArrayList<IValueExpression>();
	private final NumericConversion<Double> _wrapper = new DoubleConversion();

	public Division(IValueExpression ve1, IValueExpression ve2, IValueExpression... veArray) {
		_veList.add(ve1);
		_veList.add(ve2);
		_veList.addAll(Arrays.asList(veArray));
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<Double> newExpr) {

	}

	@Override
	public Double eval(DataTuple tuple) {
		final IValueExpression firstVE = _veList.get(0);
		final Object firstObj = firstVE.eval(tuple);
		final NumericConversion firstType = (NumericConversion) firstVE.getType();
		double result = firstType.toDouble(firstObj);

		for (int i = 1; i < _veList.size(); i++) {
			final IValueExpression currentVE = _veList.get(i);
			final Object currentObj = currentVE.eval(tuple);
			final NumericConversion currentType = (NumericConversion) currentVE.getType();
			result /= currentType.toDouble(currentObj);
		}
		return _wrapper.fromDouble(result);
	}

	@Override
	public String evalString(DataTuple tuple) {
		final Double result = eval(tuple);
		return _wrapper.toString(result);
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		return _veList;
	}

	@Override
	public TypeConversion getType() {
		return _wrapper;
	}

	@Override
	public void inverseNumber() {

	}

	@Override
	public boolean isNegative() {
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _veList.size(); i++) {
			sb.append("(").append(_veList.get(i)).append(")");
			if (i != _veList.size() - 1)
				sb.append(" / ");
		}
		return sb.toString();
	}

}