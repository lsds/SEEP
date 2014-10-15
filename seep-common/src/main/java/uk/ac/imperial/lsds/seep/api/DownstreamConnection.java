package uk.ac.imperial.lsds.seep.api;

public class DownstreamConnection {

	private LogicalOperator downstreamLogicalOperator;
	private int streamId;

	public LogicalOperator getDownstreamLogicalOperator() {
		return downstreamLogicalOperator;
	}

	public void setDownstreamLogicalOperator(LogicalOperator downstreamLogicalOperator) {
		this.downstreamLogicalOperator = downstreamLogicalOperator;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	
	public DownstreamConnection(LogicalOperator downstreamLogicalOperator, int streamId){
		this.setDownstreamLogicalOperator(downstreamLogicalOperator);
		this.setStreamId(streamId);
	}
	
}
