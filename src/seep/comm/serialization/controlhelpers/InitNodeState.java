package seep.comm.serialization.controlhelpers;

public class InitNodeState {

	private int nodeId;
	private InitOperatorState[] initOperatorState;
	//
	private int senderOperatorId;
	
	public int getSenderOperatorId() {
		return senderOperatorId;
	}
	public void setSenderOperatorId(int senderOperatorId) {
		this.senderOperatorId = senderOperatorId;
	}
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public InitOperatorState[] getInitOperatorState() {
		return initOperatorState;
	}
	public void setInitOperatorState(InitOperatorState[] initOperatorState) {
		this.initOperatorState = initOperatorState;
	}
	
	public InitNodeState(){	}
	
	public InitNodeState(int senderOperatorId, int nodeId, InitOperatorState[] initOperatorState){
		this.senderOperatorId = senderOperatorId;
		this.nodeId = nodeId;
		this.initOperatorState = initOperatorState;
	}
	
}
