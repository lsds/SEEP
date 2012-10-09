package seep.operator;

import seep.comm.serialization.controlhelpers.InitState;

/**
* StateHandler. This interface shows the methods that are operator-dependant. These must be implemented by a developer.
*/ 

public interface StatefulOperator{

	public int getCounter();
	public void generateBackupState();
	public void installState(InitState is);
	public long getBackupTime();

}
