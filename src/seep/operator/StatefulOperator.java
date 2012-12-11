package seep.operator;


public interface StatefulOperator{

	public State getState();
	public void replaceState(State state);
//	public int getCounter();
//	public void generateBackupState();
//	public void installState(StateI is);
//	public long getBackupTime();

}
