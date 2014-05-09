package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.BetweenPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.ComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.LikePredicate;
import uk.ac.imperial.lsds.streamsql.predicates.ORPredicate;

public interface PredicateVisitor {

	public void visit(ANDPredicate and);

	public void visit(BetweenPredicate between);

	public void visit(ComparisonPredicate comparison);

	public void visit(LikePredicate like);

	public void visit(ORPredicate or);

}
