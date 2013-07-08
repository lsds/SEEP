package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

public class StateAck {

	private int nodeId;
	private int mostUpstreamOpId;

	public StateAck(){
		
	}
	
	public StateAck(int nodeId, int mostUpstreamOpId){
		this.nodeId = nodeId;
		this.mostUpstreamOpId = mostUpstreamOpId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public int getMostUpstreamOpId(){
		return mostUpstreamOpId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public void setMostUpstreamOpId(int mostUpstreamOpId){
		this.mostUpstreamOpId = mostUpstreamOpId;
	}
}
