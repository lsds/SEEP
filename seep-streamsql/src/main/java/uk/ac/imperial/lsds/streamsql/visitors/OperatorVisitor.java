package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.MicroAggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.SelectionKernel;

import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;

public interface OperatorVisitor {

	public void visit (Projection projection);
	
	public void visit (Selection selection);

	public void visit (MicroAggregation aggregation);

	public void visit (ProjectionKernel projectionKernel);
	
	public void visit (SelectionKernel projectionKernel);
	
	public void visit (MicroAggregationKernel projectionKernel);
}
