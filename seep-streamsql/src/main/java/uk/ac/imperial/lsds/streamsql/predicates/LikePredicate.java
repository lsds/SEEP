package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.types.StringType;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

/*
 * ve1 LIKE ve2 (bigger smaller)
 * WORKS ONLY for pattern '%value%'
 */
public class LikePredicate implements IPredicate {
	
	private final IValueExpression<StringType> _ve1;
	private IValueExpression<StringType> _ve2;

	public LikePredicate(Constant<StringType> ve1, Constant<StringType> ve2) {
		_ve1 = ve1;
		_ve2 = ve2;
		// WORKS ONLY for pattern '%value%'
		if (_ve2 instanceof Constant) {
			StringType value = _ve2.eval(null);
			value = value.replace("%", "");
			_ve2 = new Constant<StringType>(value);
		}
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public IPredicate[] getInnerPredicates() {
		return new IPredicate[0];
	}

	@Override
	public boolean satisfied(MultiOpTuple tupleValues) {
		final StringType val1 = _ve1.eval(tupleValues);
		final StringType val2 = _ve2.eval(tupleValues);
		return val1.contains(val2);
	}

	@Override
	public boolean satisfied(MultiOpTuple firstTupleValues, MultiOpTuple secondTupleValues) {
		final StringType val1 = _ve1.eval(firstTupleValues);
		final StringType val2 = _ve2.eval(firstTupleValues);
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