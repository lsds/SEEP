package seep.comm.serialization.controlhelpers;

import seep.operator.State;

public class InitState {

	private int opId;
	private long ts;
	private State state;
	
	public InitState(){
		
	}
	
	public InitState(int opId, long ts, State state){
		this.opId = opId;
		this.ts = ts;
		this.state = state;
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
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
}
