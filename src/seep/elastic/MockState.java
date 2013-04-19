package seep.elastic;

import seep.operator.Partitionable;
import seep.operator.State;

public class MockState extends State implements Partitionable{

	private static final long serialVersionUID = 1L;

	public MockState(){}
	
	@Override
	public State[] splitState(State toSplit, int key) {
		
		return null;
	}

	@Override
	public String getKeyAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKeyAttribute(String s) {
		// TODO Auto-generated method stub
		
	}

}
