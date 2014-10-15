package uk.ac.imperial.lsds.seep.api;

import java.util.List;

public interface QueryAPI {

	public QueryBuilder queryAPI = new QueryBuilder();
	
	public List<LogicalOperator> getQueryOperators();
	public List<LogicalState> getQueryState();
	public int getInitialPhysicalInstancesPerLogicalOperator(int logicalOperatorId);
	public List<LogicalOperator> getSources();
	public LogicalOperator getSink();
	
	public LogicalOperator newStatefulSource(SeepTask seepTask, LogicalState state, int opId);
	public LogicalOperator newStatelessSource(SeepTask seepTask, int opId);
	public LogicalOperator newStatefulOperator(SeepTask seepTask, LogicalState state, int opId);
	public LogicalOperator newStatelessOperator(SeepTask seepTask, int opId);
	public LogicalOperator newStatefulSink(SeepTask seepTask, LogicalState state, int opId);
	public LogicalOperator newStatelessSink(SeepTask seepTask, int opId);
	
	public void setInitialPhysicalInstancesForLogicalOperator(int opId, int numInstances);
	
	public LogicalState newLogicalState(SeepState state, int ownerId);
	
	public String toString();
}
