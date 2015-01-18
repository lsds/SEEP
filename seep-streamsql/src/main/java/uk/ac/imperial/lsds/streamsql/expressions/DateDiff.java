package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.DateConversion;
import uk.ac.imperial.lsds.streamsql.conversion.IntegerConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class DateDiff implements IValueExpression<Integer> {
	private static final long serialVersionUID = 1L;

	private final TypeConversion<Date> _dc = new DateConversion();
	private final TypeConversion<Integer> _ic = new IntegerConversion();

	private final IValueExpression<Date> _ve1, _ve2;

	public DateDiff(IValueExpression<Date> ve1, IValueExpression<Date> ve2) {
		_ve1 = ve1;
		_ve2 = ve2;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<Integer> newExpr) {
		// nothing
	}

	@Override
	public Integer eval(DataTuple tuple) {		
		Date dateObj1 = _ve1.eval(tuple);
		Date dateObj2 = _ve2.eval(tuple);
		
		long diff = dateObj2.getTime() - dateObj1.getTime();
		int diffDays =  (int) (diff / (24* 1000 * 60 * 60));

		return diffDays;
	}

	@Override
	public String evalString(DataTuple tuple) {
		return _ic.toString(eval(tuple));
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		final List<IValueExpression> result = new ArrayList<IValueExpression>();
		result.add(_ve1);
		result.add(_ve2);
		return result;
	}

	@Override
	public TypeConversion getType() {
		return _ic;
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
		sb.append("DateDiff ");
		sb.append("First Exp: ").append(_ve1.toString()).append("\n");
		sb.append("Second Exp: ").append(_ve2.toString()).append("\n");		
		return sb.toString();
	}
}