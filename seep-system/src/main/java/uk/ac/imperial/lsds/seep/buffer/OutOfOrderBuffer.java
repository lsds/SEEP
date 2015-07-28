package uk.ac.imperial.lsds.seep.buffer;

import java.util.Iterator;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;
import static uk.ac.imperial.lsds.seep.manet.MeanderMetricsNotifier.notifyThat;

public class OutOfOrderBuffer implements IBuffer {
	private static final Logger logger = LoggerFactory.getLogger(OutOfOrderBuffer.class);
	
	private final TreeMap<Long, BatchTuplePayload> log = new TreeMap<>();
	private final int opID;
	
	public OutOfOrderBuffer(int opID)
	{
		this.opID = opID;
	}
	
	@Override
	public synchronized void save(BatchTuplePayload batch, long outputTs,
			TimestampTracker inputTs) {
		logger.trace("Saving tuple "+batch.getTuple(0).timestamp);
		log.put(batch.getTuple(0).timestamp, batch);
		notifyThat(opID).savedBatch();
	}

	@Override
	public synchronized TreeMap<Long, BatchTuplePayload> trim(FailureCtrl fctrl) {
		if (fctrl == null)
		{
			//Return and remove everything, connection has failed.
			TreeMap<Long, BatchTuplePayload> trimmed = new TreeMap<>(log);
			log.clear();
			logger.debug("Cleared log.");
			notifyThat(opID).clearedBuffer();
			return trimmed;
		}
		else
		{
			Iterator<Long> iter = log.keySet().iterator();
			while (iter.hasNext())
			{
				Long ts = iter.next();
				BatchTuplePayload batch = log.get(ts);
				batch.trim(fctrl);
				if (batch.size() <= 0) 
				{ 
					logger.trace("Trimmed batch "+ts);
					iter.remove(); 
					notifyThat(opID).trimmedBuffer(1);
				}
			}
			return null;
		}
		//TODO: This won't work with SEEPs current batching/acking model.
		//throw new RuntimeException("Need to handle (SEEP) batch sending properly here!");		
	} 
	
	public synchronized TreeMap<Long, BatchTuplePayload> get(FailureCtrl fctrl)
	{
		//TODO: Just return those in fctrl that haven't been acknowledged without deleting anything.
		return trim(null);
	}
	
	@Override
	public synchronized int size() {
		return log.size();
	}
	
	public synchronized boolean contains(long ts)
	{		
		logger.trace("Checking for "+ts+" in log: "+log.keySet());
		return log.containsKey(ts);
	}
	
	public synchronized BatchTuplePayload get(long ts)
	{
		logger.trace("Trying to get "+ts+" from log: "+log.keySet());
		return log.get(ts);
	}
	
	@Override
	public Iterator<OutputLogEntry> iterator() {
		throw new RuntimeException("Synchronization?");
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
