package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public interface IPredicate {
	
	public boolean satisfied(IQueryBuffer buffer, ITupleSchema schema, int offset);

	public boolean satisfied(IQueryBuffer firstBuffer, ITupleSchema firstSchema, int firstOffset, 
			IQueryBuffer secondBuffer, ITupleSchema secondSchema, int secondOffset);

	public void accept(PredicateVisitor pv);

	@Override
	public String toString();
	
	public int getNumPredicates();
	
}
