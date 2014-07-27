package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class ANDPredicate implements IPredicate {
	
	IPredicate[] predicates;

	public ANDPredicate(IPredicate... predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean satisfied(MultiOpTuple tuple) {
		for (IPredicate pred : predicates)
			if (!pred.satisfied(tuple))
				return false;
		return true;
	}

	@Override
	public boolean satisfied(MultiOpTuple firstTuple, MultiOpTuple secondTuple) {
		for (IPredicate pred : predicates)
			if (!pred.satisfied(firstTuple,secondTuple))
				return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.predicates.length; i++) {
			sb.append("(").append(predicates[i]).append(")");
			if (i != predicates.length - 1)
				sb.append(" AND ");
		}
		return sb.toString();
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public IPredicate[] getInnerPredicates() {
		return predicates;
	}


}