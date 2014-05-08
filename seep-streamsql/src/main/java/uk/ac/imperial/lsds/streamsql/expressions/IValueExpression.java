package uk.ac.imperial.lsds.streamsql.expressions;

import java.io.Serializable;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public interface IValueExpression<T extends Comparable<T>> extends Serializable {
	
	public void accept(ValueExpressionVisitor vev);

	public void changeValues(int i, IValueExpression<T> newExpr);

	public T eval(DataTuple tuple);

	public String evalString(DataTuple tuple);

	// not ValueExpression<T> because inside might be other type(as in
	// IntegerYearFromDate)
	public List<IValueExpression> getInnerExpressions();

	public TypeConversion getType();

	public void inverseNumber();

	public boolean isNegative();
}