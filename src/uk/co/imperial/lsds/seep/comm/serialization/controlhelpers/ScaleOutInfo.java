package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

public class ScaleOutInfo {
	
	private int oldOpId;
	private int newOpId;
	private boolean isStateful;
	
	public ScaleOutInfo(){
		
	}
	
	public ScaleOutInfo(int oldOpId, int newOpId, boolean isStateful){
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
		this.isStateful = isStateful;
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
	
	public boolean isStatefulScaleOut(){
		return isStateful;
	}
}
