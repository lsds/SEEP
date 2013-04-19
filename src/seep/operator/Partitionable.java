package seep.operator;

public interface Partitionable {

	public void setKeyAttribute(String keyAttribute);
	public String getKeyAttribute();
	public State[] splitState(State toSplit, int key);
	
}
