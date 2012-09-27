package seep.operator;

import seep.comm.serialization.controlhelpers.BackupState;


public interface StateSplitI {
	
	public BackupState[] parallelizeState(BackupState toSplit, int key);
	
}
