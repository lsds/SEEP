package seep.elastic;

import java.util.ArrayList;

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

	@Override
	public void setDirtyMode(boolean newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconcile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Object> streamSplitState(State toSplit, int iteration) {
		// TODO Auto-generated method stub
		return null;
	}

}
