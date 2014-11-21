package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.ORPredicate;

public interface PredicateVisitor {

	public void visit(ANDPredicate and);

	public void visit(IntComparisonPredicate comparison);

	public void visit(FloatComparisonPredicate comparison);

	public void visit(ORPredicate or);

}
