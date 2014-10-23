package uk.ac.imperial.lsds.seep.api;

import java.util.List;

public interface QueryAPI {
	
	public List<Operator> getQueryOperators();
	public List<LogicalState> getQueryState();
	public int getInitialPhysicalInstancesPerLogicalOperator(int logicalOperatorId);
	public List<Operator> getSources();
	public Operator getSink();
	
	public Operator newStatefulSource(SeepTask seepTask, LogicalState state, int opId);
	public Operator newStatelessSource(SeepTask seepTask, int opId);
	public Operator newStatefulOperator(SeepTask seepTask, LogicalState state, int opId);
	public Operator newStatelessOperator(SeepTask seepTask, int opId);
	public Operator newStatefulSink(SeepTask seepTask, LogicalState state, int opId);
	public Operator newStatelessSink(SeepTask seepTask, int opId);
	
	public void setInitialPhysicalInstancesForLogicalOperator(int opId, int numInstances);
	
	public LogicalState newLogicalState(SeepState state, int ownerId);
	
	public String toString();
}
