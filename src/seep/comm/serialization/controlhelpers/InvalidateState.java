package seep.comm.serialization.controlhelpers;

public class InvalidateState {

	private int opId;

	public InvalidateState(){
	}
	
	public InvalidateState(int opId){
		this.opId = opId;
	}
	
	public int getOpId() {
		return opId;
	}

	public void setOpId(int opId) {
		this.opId = opId;
	}
	
}
