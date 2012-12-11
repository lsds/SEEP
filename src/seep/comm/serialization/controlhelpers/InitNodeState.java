package seep.comm.serialization.controlhelpers;

public class InitNodeState {

	private int nodeId;
	private InitOperatorState[] initOperatorState;
	
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
	
	public InitNodeState(int nodeId, InitOperatorState[] initOperatorState){
		this.nodeId = nodeId;
		this.initOperatorState = initOperatorState;
	}
	
}
