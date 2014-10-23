package uk.ac.imperial.lsds.seep.api;

import java.util.List;

public interface Operator {

	// id, name and type (stateful or stateless)
	public int getOperatorId();
	public String getOperatorName();
	public boolean isStateful();
	public LogicalState getState();
	// task
	public SeepTask getSeepTask();
	// connections to other logical operators
	public List<DownstreamConnection> downstreamConnections();
	public List<UpstreamConnection> upstreamConnections();
	// methods to connect LogicalOperator to other operators
	public void connectTo(Operator downstreamOperator, int streamId);
	public void connectTo(Operator downstreamOperator, int streamId, ConnectionType connectionType);
	
	public String toString();
	
}
