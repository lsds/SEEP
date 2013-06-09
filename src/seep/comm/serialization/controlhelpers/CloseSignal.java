package seep.comm.serialization.controlhelpers;

public class CloseSignal {

	private int opId;
	
	public CloseSignal(){}
	
	public CloseSignal(int opId){
		this.opId = opId;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
}
