package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;

import uk.co.imperial.lsds.seep.operator.State;

public class BackupOperatorState implements Serializable{

	private static final long serialVersionUID = 1L;

	private int opId;
	
	private State state;
	private String stateClass;
	
	public BackupOperatorState(){}
	
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