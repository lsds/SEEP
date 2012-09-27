package seep.comm.serialization.controlhelpers;

public class StateAck {

	private int opId;

	public StateAck(){
		
	}
	
	public StateAck(int opId){
		this.opId = opId;
	}
	
	public int getOpId() {
		return opId;
	}

	public void setOpId(int opId) {
		this.opId = opId;
	}
}
