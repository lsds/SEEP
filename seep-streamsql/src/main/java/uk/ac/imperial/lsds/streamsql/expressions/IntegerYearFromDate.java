package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.IntegerConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class IntegerYearFromDate implements IValueExpression<Integer> {
	private static final long serialVersionUID = 1L;

	private final IValueExpression<Date> _veDate;
	private final TypeConversion<Integer> _wrapper = new IntegerConversion();

	public IntegerYearFromDate(IValueExpression<Date> veDate) {
		_veDate = veDate;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	/*
	 * @Override public Integer eval(List<String> firstTuple, List<String>
	 * secondTuple) { Date date = _veDate.eval(firstTuple, secondTuple);
	 * Calendar c = Calendar.getInstance(); c.setTime(date); int year =
	 * c.get(Calendar.YEAR);
	 * // Alternative approach: //SimpleDateFormat formatNowYear = new
	 * SimpleDateFormat("yyyy"); //String currentYear =
	 * formatNowYear.format(date); // = '2006'
	 * return year; }
	 */

	@Override
	public void changeValues(int i, IValueExpression<Integer> newExpr) {
		// nothing

	}

	@Override
	public Integer eval(DataTuple tuple) {
		final Date date = _veDate.eval(tuple);

		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int year = c.get(Calendar.YEAR);

		/*
		 * Alternative approach: SimpleDateFormat formatNowYear = new
		 * SimpleDateFormat("yyyy"); String currentYear =
		 * formatNowYear.format(date); // = '2006'
		 */

		return year;
	}

	@Override
	public String evalString(DataTuple tuple) {
		final int result = eval(tuple);
		return _wrapper.toString(result);
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		final List<IValueExpression> result = new ArrayList<IValueExpression>();
		result.add(_veDate);
		return result;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("IntegerYearFromDate ").append(_veDate.toString());
		return sb.toString();
	}

}