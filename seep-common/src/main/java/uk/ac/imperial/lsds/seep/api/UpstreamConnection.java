package uk.ac.imperial.lsds.seep.api;

public class UpstreamConnection {

	private LogicalOperator upstreamLogicalOperator;
	private ConnectionType connectionType;
	private int streamId;

	public LogicalOperator getUpstreamLogicalOperator() {
		return upstreamLogicalOperator;
	}

	public void setUpstreamLogicalOperator(LogicalOperator upstreamLogicalOperator) {
		this.upstreamLogicalOperator = upstreamLogicalOperator;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	
	public UpstreamConnection(LogicalOperator upstreamLogicalOperator, ConnectionType connectionType, int streamId){
		this.setUpstreamLogicalOperator(upstreamLogicalOperator);
		this.setConnectionType(connectionType);
		this.setStreamId(streamId);
	}
	
}
