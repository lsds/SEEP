package uk.ac.imperial.lsds.seep.api;

public class SeepQueryOperatorState implements LogicalState{

	private int ownerId;
	private SeepState state;
	
	private SeepQueryOperatorState(SeepState state, int ownerId) {
		this.state = state;
		this.ownerId = ownerId;
	}
	
	public static LogicalState newState(SeepState state, int ownerId) {
		return new SeepQueryOperatorState(state, ownerId);
	}
	
	@Override
	public int getStateId() {
		return ownerId;
	}

	@Override
	public SeepState getStateElement() {
		return state;
	}

}
