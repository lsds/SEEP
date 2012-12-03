package seep.operator;

import seep.comm.serialization.controlhelpers.StateI;

public abstract class State {

	private int ownerId;
	private String stateTag;
	private State stateImpl;
	
	public void setStateTag(String stateTag){
		this.stateTag = stateTag;
	}
	
	public String getStateTag(){
		return stateTag;
	}
	
	public void setOwnerId(int ownerId){
		this.ownerId = ownerId;
	}
	
	public int getOwnerId(){
		return ownerId;
	}
	
	public abstract StateI[] splitState(StateI toSplit, int key);
	public abstract int getCounter();
	public abstract void generateBackupState();
	public abstract void installState(StateI is);
	public abstract long getBackupTime();
	
}
