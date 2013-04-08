package seep.comm.serialization.controlhelpers;

public class InvalidateState {

	private int operatorId;

	public InvalidateState(){
	}
	
	public InvalidateState(int nodeId){
		this.operatorId = nodeId;
	}
	
	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int nodeId) {
		this.operatorId = nodeId;
	}
}
