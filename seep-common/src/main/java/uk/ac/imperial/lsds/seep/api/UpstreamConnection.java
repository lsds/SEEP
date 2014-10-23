package uk.ac.imperial.lsds.seep.api;

public class UpstreamConnection {

	private Operator upstreamOperator;
	private ConnectionType connectionType;
	private int streamId;

	public Operator getUpstreamOperator() {
		return upstreamOperator;
	}

	public void setUpstreamOperator(Operator upstreamOperator) {
		this.upstreamOperator = upstreamOperator;
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
	
	public UpstreamConnection(Operator upstreamOperator, ConnectionType connectionType, int streamId){
		this.setUpstreamOperator(upstreamOperator);
		this.setConnectionType(connectionType);
		this.setStreamId(streamId);
	}
	
	public void replaceOperator(Operator upstreamOperator) {
		this.upstreamOperator = upstreamOperator;
	}
	
}
