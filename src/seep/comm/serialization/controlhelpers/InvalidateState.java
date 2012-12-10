package seep.comm.serialization.controlhelpers;

public class InvalidateState {

	private int nodeId;

	public InvalidateState(){
	}
	
	public InvalidateState(int nodeId){
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
}
