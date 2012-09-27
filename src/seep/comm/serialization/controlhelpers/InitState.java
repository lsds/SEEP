package seep.comm.serialization.controlhelpers;

public class InitState {

	private int opId;
	private long ts;
	private StateI state;
	
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
