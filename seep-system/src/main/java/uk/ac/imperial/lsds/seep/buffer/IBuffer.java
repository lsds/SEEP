package uk.ac.imperial.lsds.seep.buffer;

import java.util.Iterator;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

public interface IBuffer {

	public abstract Iterator<OutputLogEntry> iterator();

	public abstract int size();

	public abstract int numTuples();

	public abstract BackupOperatorState getBackupState();

	public abstract void replaceBackupOperatorState(BackupOperatorState bs);

	public abstract void replaceRawData(RawData rw);

	public abstract void save(BatchTuplePayload batch, long outputTs,
			TimestampTracker inputTs);

	public abstract TimestampTracker trim(long ts);

	public abstract void trim(FailureCtrl fctrl);

	///fixme{just for testing, do binary search on structure}
	public abstract TimestampTracker getInputVTsForOutputTs(long output_ts);

}