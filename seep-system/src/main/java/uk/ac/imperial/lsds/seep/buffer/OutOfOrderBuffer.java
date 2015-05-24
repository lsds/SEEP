package uk.ac.imperial.lsds.seep.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

public class OutOfOrderBuffer implements IBuffer {
	private static final Logger logger = LoggerFactory.getLogger(OutOfOrderBuffer.class);
	
	private final Map<Long, OutputLogEntry> log = new HashMap<>();	//TODO: Will probably want to change this.

	@Override
	public Iterator<OutputLogEntry> iterator() {
		throw new RuntimeException("Synchronization?");
	}

	@Override
	public synchronized int size() {
		return log.size();
	}

	@Override
	public synchronized int numTuples() {
		int total = 0;
		for (OutputLogEntry o : log.values())
		{
			total += o.batch.size();
		}
		logger.error("TODO: Tmp hack, not thread safe.");
		return total;
	}

	@Override
	public synchronized void save(BatchTuplePayload batch, long outputTs,
			TimestampTracker inputTs) {
		log.put(outputTs, new OutputLogEntry(outputTs, inputTs, batch));		
	}

	@Override
	public synchronized List<OutputLogEntry> trim(FailureCtrl fctrl) {
		if (fctrl == null)
		{
			//Return and remove everything, connection has failed.
			List<OutputLogEntry> trimmed = new ArrayList<>(log.values());
			log.clear();
			return trimmed;
		}
		else
		{
			Iterator<Long> iter = log.keySet().iterator();
			while (iter.hasNext())
			{
				BatchTuplePayload batch = log.get(iter.next()).batch;
				batch.trim(fctrl);
				if (batch.size() <= 0) { iter.remove(); }
			}
			return null;
		}
		//TODO: This won't work with SEEPs current batching/acking model.
		//throw new RuntimeException("Need to handle (SEEP) batch sending properly here!");		
	}
	
	@Override
	public BackupOperatorState getBackupState() {
		throw new RuntimeException("Logic error.");
	}

	@Override
	public void replaceBackupOperatorState(BackupOperatorState bs) {
		throw new RuntimeException("Logic error.");		
	}

	@Override
	public void replaceRawData(RawData rw) {
		throw new RuntimeException("Logic error.");		
	}
	
	@Override
	public TimestampTracker trim(long ts) {
		throw new RuntimeException("Logic error.");
	}

	@Override
	public TimestampTracker getInputVTsForOutputTs(long output_ts) {
		throw new RuntimeException("Logic error.");
	}

}
