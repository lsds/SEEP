package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class ORPredicate implements IPredicate {
	
	IPredicate[] predicates;

	public ORPredicate(IPredicate... predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean satisfied(IQueryBuffer buffer, TupleSchema schema, int offset) {
		for (IPredicate pred : predicates)
			if (pred.satisfied(buffer, schema, offset))
				return true;
		return false;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.predicates.length; i++) {
			sb.append("(").append(predicates[i]).append(")");
			if (i != predicates.length - 1)
				sb.append(" OR ");
		}
		return sb.toString();
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

}