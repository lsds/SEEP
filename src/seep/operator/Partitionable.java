package seep.operator;

import java.util.ArrayList;

public interface Partitionable {

	public void setDirtyMode(boolean newValue);
	public void reconcile();
	public int getSize();
	public void setKeyAttribute(String keyAttribute);
	public String getKeyAttribute();
	public State[] splitState(State toSplit, int key);
	public ArrayList<Object> streamSplitState(State toSplit, int iteration);
	
}
