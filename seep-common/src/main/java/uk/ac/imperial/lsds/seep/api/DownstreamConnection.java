package uk.ac.imperial.lsds.seep.api;

public class DownstreamConnection {

	private Operator downstreamOperator;
	private int streamId;

	public Operator getDownstreamOperator() {
		return downstreamOperator;
	}

	public void setDownstreamOperator(Operator downstreamOperator) {
		this.downstreamOperator = downstreamOperator;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	
	public DownstreamConnection(Operator downstreamOperator, int streamId){
		this.setDownstreamOperator(downstreamOperator);
		this.setStreamId(streamId);
	}
	
	public void replaceOperator(Operator replacement){
		this.downstreamOperator = replacement;
	}
	
}
