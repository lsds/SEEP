package seep.operator;

import seep.comm.tuples.Seep.BackupState;

public interface StateSplitI {
	
	public BackupState.Builder[] parallelizeState(BackupState toSplit, int key);
	
}
