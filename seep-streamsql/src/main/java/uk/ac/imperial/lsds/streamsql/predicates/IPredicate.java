package uk.ac.imperial.lsds.streamsql.predicates;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public interface IPredicate {
	
	public boolean satisfied(DataTuple tuple);

	public boolean satisfied(DataTuple firstTuple, DataTuple secondTuple);

	public void accept(PredicateVisitor pv);

	public List<IPredicate> getInnerPredicates();

	@Override
	public String toString();
	
}
