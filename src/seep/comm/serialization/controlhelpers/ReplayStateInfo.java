package seep.comm.serialization.controlhelpers;

public class ReplayStateInfo {

	private int oldOpId;
	private int newOpId;
	private boolean streamToSingleNode;
	
	public ReplayStateInfo(){}
	
	public ReplayStateInfo(int oldOpId, int newOpId, boolean singleNode){
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
		this.streamToSingleNode = singleNode;
	}
	
	public int getOldOpId() {
		return oldOpId;
	}

	public void setOldOpId(int oldOpId) {
		this.oldOpId = oldOpId;
	}

	public int getNewOpId() {
		return newOpId;
	}

	public void setNewOpId(int newOpId) {
		this.newOpId = newOpId;
	}

	public boolean isStreamToSingleNode() {
		return streamToSingleNode;
	}

	public void setSingleNode(boolean singleNode) {
		this.streamToSingleNode = singleNode;
	}
}
