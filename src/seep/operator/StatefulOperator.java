package seep.operator;


public interface StatefulOperator{

	public State getState();
	public void replaceState(State state);

}
