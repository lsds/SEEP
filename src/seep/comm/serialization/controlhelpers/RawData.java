package seep.comm.serialization.controlhelpers;

public class RawData {

	private int opId;
	private long ts;
	private byte[] data;
	
	public RawData(){}
	
	public RawData(int nodeId, byte[] data){
		this.opId = nodeId;
		this.data = data;
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
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
}
