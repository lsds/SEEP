package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;


/* This class is syntactic sugar for complex AndPredicate
 */
public class BetweenPredicate<T extends PrimitiveType> implements IPredicate {

	private final IPredicate _and;

	public BetweenPredicate(Constant<T> ve, boolean includeLower,
			Constant<T> veLower, boolean includeUpper, Constant<T> veUpper) {

		// set up boundaries correctly
		int opLower = ComparisonPredicate.GREATER_OP;
		if (includeLower)
			opLower = ComparisonPredicate.NONLESS_OP;
		int opUpper = ComparisonPredicate.LESS_OP;
		if (includeUpper)
			opUpper = ComparisonPredicate.NONGREATER_OP;

		// create syntactic sugar
		final IPredicate lower = new ComparisonPredicate(opLower, ve, veLower);
		final IPredicate upper = new ComparisonPredicate(opUpper, ve, veUpper);
		_and = new ANDPredicate(lower, upper);
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public IPredicate[] getInnerPredicates() {
		return new IPredicate[] {_and};
	}

	@Override
	public boolean satisfied(MultiOpTuple tupleValues) {
		return _and.satisfied(tupleValues);
	}

	@Override
	public boolean satisfied(MultiOpTuple firstTupleValues, MultiOpTuple secondTupleValues) {
		return _and.satisfied(firstTupleValues, secondTupleValues);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BETWEEN implemented as AND: ").append(_and.toString());
		return sb.toString();
	}

}