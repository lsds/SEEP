package seep.comm.serialization.controlhelpers;

import seep.operator.State;

public class BackupState {

	private int opId;
	
	private State state;
	private String stateClass;
	
	public void setStateClass(String stateClass){
		this.stateClass = stateClass;
	}
	public String getStateClass(){
		return stateClass;
	}
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
}
