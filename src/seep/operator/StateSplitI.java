package seep.operator;



public interface StateSplitI {
	
	public State[] parallelizeState(State toSplit, int key, String stateClass);
	
}
