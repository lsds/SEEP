package seep.comm.serialization.controlhelpers;

public class InitState {

	private int opId;
	private long ts;
	private StateI state;
	
	public InitState(){
		
	}
	
	public InitState(int opId, long ts, StateI state){
		this.opId = opId;
		this.ts = ts;
		this.state = state;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
	public StateI getState() {
		return state;
	}
	public void setState(StateI state) {
		this.state = state;
	}
}
