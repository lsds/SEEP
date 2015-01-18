package uk.ac.imperial.lsds.streamsql.predicates;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

/*
 * ve1 LIKE ve2 (bigger smaller)
 * WORKS ONLY for pattern '%value%'
 */
public class LikePredicate implements IPredicate {
	
	private static final long serialVersionUID = 1L;

	private final IValueExpression<String> _ve1;
	private IValueExpression<String> _ve2;

	public LikePredicate(ValueExpression<String> ve1, ValueExpression<String> ve2) {
		_ve1 = ve1;
		_ve2 = ve2;
		// WORKS ONLY for pattern '%value%'
		if (_ve2 instanceof ValueExpression) {
			String value = _ve2.eval(null);
			value = value.replace("%", "");
			_ve2 = new ValueExpression<String>(new StringConversion(), value);
		}
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	public List<IValueExpression<String>> getExpressions() {
		final List<IValueExpression<String>> result = new ArrayList<IValueExpression<String>>();
		result.add(_ve1);
		result.add(_ve2);
		return result;
	}

	@Override
	public List<IPredicate> getInnerPredicates() {
		return new ArrayList<IPredicate>();
	}

	@Override
	public boolean satisfied(DataTuple tupleValues) {
		final String val1 = _ve1.eval(tupleValues);
		final String val2 = _ve2.eval(tupleValues);
		return val1.contains(val2);
	}

	@Override
	public boolean satisfied(DataTuple firstTupleValues, DataTuple secondTupleValues) {
		final String val1 = _ve1.eval(firstTupleValues);
		final String val2 = _ve2.eval(firstTupleValues);
		return val1.contains(val2);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(_ve1.toString());
		sb.append(" LIKE ");
		sb.append(_ve2.toString());
		return sb.toString();
	}

}