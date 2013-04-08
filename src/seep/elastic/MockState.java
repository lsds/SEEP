package seep.elastic;

import seep.operator.State;

public class MockState extends State{

	private static final long serialVersionUID = 1L;

	public MockState(){}
	
	@Override
	public State[] splitState(State toSplit, int key) {
		
		return null;
	}

}
