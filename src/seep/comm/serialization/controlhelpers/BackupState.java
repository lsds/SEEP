package seep.comm.serialization.controlhelpers;

public class BackupState {

	private int opId;
	private long ts_s;
	private long ts_e;
	private StateI state;
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public long getTs_s() {
		return ts_s;
	}
	public void setTs_s(long tsS) {
		ts_s = tsS;
	}
	public long getTs_e() {
		return ts_e;
	}
	public void setTs_e(long tsE) {
		ts_e = tsE;
	}
	public StateI getState() {
		return state;
	}
	public void setState(StateI state) {
		this.state = state;
	}
}
