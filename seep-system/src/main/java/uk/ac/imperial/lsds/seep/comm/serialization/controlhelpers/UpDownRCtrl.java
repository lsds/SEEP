package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

public class UpDownRCtrl {
	private int opId;
	private int qlen;
	
	public UpDownRCtrl() {}
	
	public UpDownRCtrl(int opId, int qlen) {
		this.opId = opId;
		this.qlen = qlen;
	}
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public int getQlen() {
		return qlen;
	}
	public void setQlen(int qlen) {
		this.qlen = qlen;
	}	
	
	public String toString() { return "upOp="+opId+",qlen="+qlen; }
}
