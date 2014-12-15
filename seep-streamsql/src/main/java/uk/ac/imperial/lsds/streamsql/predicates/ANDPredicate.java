package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class ANDPredicate implements IPredicate {
	
	IPredicate[] predicates;

	public ANDPredicate(IPredicate... predicates) {
		this.predicates = predicates;
	}
	
	@Override
	public boolean satisfied(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		for (IPredicate pred : predicates)
			if (!pred.satisfied(buffer, schema, offset))
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
	public boolean satisfied(IQueryBuffer firstBuffer,
			ITupleSchema firstSchema, int firstOffset,
			IQueryBuffer secondBuffer, ITupleSchema secondSchema,
			int secondOffset) {
		for (IPredicate pred : predicates)
			if (!pred.satisfied(firstBuffer, firstSchema, firstOffset, secondBuffer, secondSchema, secondOffset))
				return false;
		return true;
	}

}