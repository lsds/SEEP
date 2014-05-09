package uk.ac.imperial.lsds.streamsql.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.util.Util;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class StringConcatenate implements IValueExpression<String> {
	private static final long serialVersionUID = 1L;

	private final List<IValueExpression<String>> _strList = new ArrayList<IValueExpression<String>>();

	public StringConcatenate(IValueExpression<String> str1, IValueExpression<String> str2,
			IValueExpression<String>... strArray) {
		_strList.add(str1);
		_strList.add(str2);
		_strList.addAll(Arrays.asList(strArray));
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void changeValues(int i, IValueExpression<String> newExpr) {

	}

	@Override
	public String eval(DataTuple tuple) {
		String result = "";
		for (final IValueExpression<String> str : _strList)
			result += str;
		return result;
	}

	@Override
	public String evalString(DataTuple tuple) {
		return eval(tuple);
	}

	@Override
	public List<IValueExpression> getInnerExpressions() {
		return Util.listTypeErasure(_strList);
	}

	@Override
	public TypeConversion getType() {
		return new StringConversion();
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
		for (int i = 0; i < _strList.size(); i++) {
			sb.append("(").append(_strList.get(i)).append(")");
			if (i != _strList.size() - 1)
				sb.append(" STR_CONCAT ");
		}
		return sb.toString();
	}

}