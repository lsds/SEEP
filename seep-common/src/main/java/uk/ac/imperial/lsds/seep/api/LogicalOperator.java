package uk.ac.imperial.lsds.seep.api;

import java.util.List;

/**
 * A logicalOperator describes all the information required to create a TaskElement that can run in a physical 
 * Seep Worker. This entity is used while building and submitting a query.
 * @author raulcf
 *
 */

public interface LogicalOperator {

	// id, name and type (stateful or stateless)
	public int getLogicalOperatorId();
	public String getLogicalOperatorName();
	public boolean isStateful();
	public LogicalState getState();
	// task
	public SeepTask getSeepTask();
	// connections to other logical operators
	public List<DownstreamConnection> downstreamConnections();
	public List<UpstreamConnection> upstreamConnections();
	// methods to connect LogicalOperator to other operators
	public void connectTo(LogicalOperator downstreamOperator, int streamId);
	public void connectTo(LogicalOperator downstreamOperator, int streamId, ConnectionType connectionType);
	
	public String toString();
	
}
