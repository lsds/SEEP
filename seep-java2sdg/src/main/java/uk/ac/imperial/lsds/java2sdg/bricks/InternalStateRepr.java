package uk.ac.imperial.lsds.java2sdg.bricks;

import soot.SootClass;

public class InternalStateRepr {

	public enum StateLabel{
		PARTITIONED, PARTIAL
	}
	
	private final int seId;
	private final SootClass stateClass;
	private final StateLabel stateLabel;
	
	public InternalStateRepr(SootClass stateClass, StateLabel stateLabel, int seId){
		this.seId = seId;
		this.stateClass = stateClass;
		this.stateLabel = stateLabel;
	}
	
	public int getSeId(){
		return seId;
	}
	
	public SootClass getStateClass(){
		return stateClass;
	}
	
	public StateLabel getStateLabel(){
		return stateLabel;
	}
}