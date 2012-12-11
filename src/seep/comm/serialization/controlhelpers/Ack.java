package seep.comm.serialization.controlhelpers;

public class Ack {

	private int opId;
	private long ts;
	
	public Ack(){}
	
	public Ack(int nodeId, long ts){
		this.opId = nodeId;
		this.ts = ts;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int nodeId) {
		this.opId = nodeId;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
}
