package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public interface IPredicate {
	
	public boolean satisfied(MultiOpTuple tuple);

	public boolean satisfied(MultiOpTuple firstTuple, MultiOpTuple secondTuple);

	public void accept(PredicateVisitor pv);

	public IPredicate[] getInnerPredicates();

	@Override
	public String toString();
	
}
