package seep.comm.serialization.controlhelpers;

public class OpenSignal {

	private int opId;
	
	public OpenSignal(){}
	
	public OpenSignal(int opId){
		this.opId = opId;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
}
