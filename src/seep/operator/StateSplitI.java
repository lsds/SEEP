package seep.operator;

import seep.comm.serialization.controlhelpers.StateI;


public interface StateSplitI {
	
	public StateI[] parallelizeState(StateI toSplit, int key, String stateClass);
	
}
