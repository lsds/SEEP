package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateful.MicroAggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.AggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ReductionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.SimpleThetaJoinKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.ThetaJoinKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless.JNIProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless.ProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless.SelectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.AProjectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.ASelectionKernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.DummyKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;

public interface OperatorVisitor {

	public void visit (Projection projection);
	
	public void visit (Selection selection);

	public void visit (ThetaJoin join);

	public void visit (MicroAggregation aggregation);

	public void visit (ProjectionKernel projectionKernel);
	
	public void visit (MicroAggregationKernel projectionKernel);

	public void visit(JNIProjectionKernel jniProjectionKernel);

	public void visit(AProjectionKernel aProjectionKernel);

	public void visit(ReductionKernel reductionKernel);

	public void visit(SelectionKernel selectionKernel);

	public void visit(ASelectionKernel aSelectionKernel);

	public void visit(AggregationKernel aggregationKernel);

	public void visit(DummyKernel dummyKernel);

	public void visit(ThetaJoinKernel thetaJoinKernel);

	public void visit(SimpleThetaJoinKernel simpleThetaJoinKernel);
}
