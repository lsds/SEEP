package seep.comm.serialization.controlhelpers;

import seep.operator.State;

public class InitOperatorState {

	private int opId;
	private State state;
	
	public InitOperatorState(){
		
	}
	
	public InitOperatorState(int opId, State state){
		this.opId = opId;
		this.state = state;
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
