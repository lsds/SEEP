package uk.ac.imperial.lsds.seep.api;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.data.Schema;

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
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema);
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType);
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType, DataOrigin dSrc);
	
	public String toString();
	
}
