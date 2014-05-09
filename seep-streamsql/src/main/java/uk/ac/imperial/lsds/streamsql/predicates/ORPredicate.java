package uk.ac.imperial.lsds.streamsql.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class ORPredicate implements IPredicate {
	
	List<IPredicate> predicates = new ArrayList<IPredicate>();

	public ORPredicate(IPredicate pred1, IPredicate pred2, IPredicate... predicates) {
		this.predicates.add(pred1);
		this.predicates.add(pred2);
		this.predicates.addAll(Arrays.asList(predicates));
	}

	@Override
	public boolean satisfied(DataTuple tuple) {
		for (IPredicate pred : predicates)
			if (pred.satisfied(tuple))
				return true;
		return false;
	}

	@Override
	public boolean satisfied(DataTuple firstTuple, DataTuple secondTuple) {
		for (IPredicate pred : predicates)
			if (pred.satisfied(firstTuple,secondTuple))
				return true;
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.predicates.size(); i++) {
			sb.append("(").append(predicates.get(i)).append(")");
			if (i != predicates.size() - 1)
				sb.append(" OR ");
		}
		return sb.toString();
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public List<IPredicate> getInnerPredicates() {
		return predicates;
	}


}