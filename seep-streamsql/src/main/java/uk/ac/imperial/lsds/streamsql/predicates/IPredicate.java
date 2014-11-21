package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public interface IPredicate {
	
	public boolean satisfied(IQueryBuffer buffer, TupleSchema schema, int offset);

	public void accept(PredicateVisitor pv);

	@Override
	public String toString();
	
}
