package seep.comm.serialization.controlhelpers;

public class ScaleOutInfo {
	
	private int oldOpId;
	private int newOpId;
	
	public ScaleOutInfo(){
		
	}
	
	public ScaleOutInfo(int oldOpId, int newOpId){
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
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
}
