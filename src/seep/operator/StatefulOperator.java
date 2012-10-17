package seep.operator;

import seep.comm.serialization.controlhelpers.StateI;

/**
* StateHandler. This interface shows the methods that are operator-dependant. These must be implemented by a developer.
*/ 

public interface StatefulOperator{

	public int getCounter();
	public void generateBackupState();
	public void installState(StateI is);
	public long getBackupTime();

}
