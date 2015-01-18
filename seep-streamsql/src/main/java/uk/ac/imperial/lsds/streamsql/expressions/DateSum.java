package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.DateConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class DateSum implements IValueExpression<Date> {
	private static final long serialVersionUID = 1L;

	private final TypeConversion<Date> _dc = new DateConversion();

	private final IValueExpression<Date> _ve;
	private final int _interval, _unit;

	public DateSum(IValueExpression<Date> ve, int unit, int interval) {
		_ve = ve;
		_unit = unit;
		_interval = interval;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<Date> newExpr) {
		// nothing
	}

	@Override
	public Date eval(DataTuple tuple) {
		final Date base = _ve.eval(tuple);
		final Calendar c = Calendar.getInstance();
		c.setTime(base);
		c.add(_unit, _interval);
		return c.getTime();
	}

	@Override
	public String evalString(DataTuple tuple) {
		return _dc.toString(eval(tuple));
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		final List<IValueExpression> result = new ArrayList<IValueExpression>();
		result.add(_ve);
		return result;
	}

	@Override
	public TypeConversion getType() {
		return _dc;
	}

	@Override
	public void inverseNumber() {
		// nothing
	}

	@Override
	public boolean isNegative() {
		// nothing
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("DateSum ").append(_ve.toString());
		sb.append(" interval ").append(_interval);
		sb.append(" unit ").append(_unit);
		return sb.toString();
	}

}