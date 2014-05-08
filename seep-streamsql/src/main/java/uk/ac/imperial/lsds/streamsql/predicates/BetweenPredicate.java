package uk.ac.imperial.lsds.streamsql.predicates;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;
import uk.ac.imperial.lsds.streamsql.visitors.SeepSQLVisitor;


/* This class is syntactic sugar for complex AndPredicate
 */
public class BetweenPredicate<T extends Comparable<T>> implements IPredicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOG = LoggerFactory.getLogger(SeepSQLVisitor.class);

	private final IPredicate _and;

	public BetweenPredicate(ValueExpression<T> ve, boolean includeLower,
			ValueExpression<T> veLower, boolean includeUpper, ValueExpression<T> veUpper) {

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
	public List<IPredicate> getInnerPredicates() {
		final List<IPredicate> result = new ArrayList<IPredicate>();
		result.add(_and);
		return result;
	}

	@Override
	public boolean satisfied(DataTuple tupleValues) {
		return _and.satisfied(tupleValues);
	}

	@Override
	public boolean satisfied(DataTuple firstTupleValues, DataTuple secondTupleValues) {
		return _and.satisfied(firstTupleValues, secondTupleValues);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BETWEEN implemented as AND: ").append(_and.toString());
		return sb.toString();
	}

}