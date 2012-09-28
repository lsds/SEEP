package seep.comm.serialization.controlhelpers;

public class Ack {

	private int opId;
	private long ts;
	
	public Ack(){}
	
	public Ack(int opId, long ts){
		this.opId = opId;
		this.ts = ts;
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
}
