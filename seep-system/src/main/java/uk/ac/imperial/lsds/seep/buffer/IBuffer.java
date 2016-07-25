package uk.ac.imperial.lsds.seep.buffer;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

public interface IBuffer {

	public abstract Iterator<OutputLogEntry> iterator();

	public abstract int size();
	
	public abstract boolean contains(long ts);

	public abstract BackupOperatorState getBackupState();

	public abstract void replaceBackupOperatorState(BackupOperatorState bs);

	public abstract void replaceRawData(RawData rw);

	public abstract void save(BatchTuplePayload batch, long outputTs,
			TimestampTracker inputTs);

	public abstract TimestampTracker trim(long ts);

	public abstract TreeMap<Long, BatchTuplePayload> trim(FailureCtrl fctrl);
	
	public abstract TreeMap<Long, BatchTuplePayload> get(FailureCtrl fctrl);
	public abstract BatchTuplePayload get(long ts);

	///fixme{just for testing, do binary search on structure}
	public abstract TimestampTracker getInputVTsForOutputTs(long output_ts);

}
