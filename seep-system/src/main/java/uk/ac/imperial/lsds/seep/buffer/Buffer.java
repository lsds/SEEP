/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.buffer;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

/**
* Buffer class models the buffers for the connections between operators in our system
*/

public class Buffer implements Serializable, IBuffer{

	private static final Logger logger = LoggerFactory.getLogger(Buffer.class);
	private static final long serialVersionUID = 1L;

//	private Deque<BatchTuplePayload> buff = new LinkedBlockingDeque<BatchTuplePayload>();
	
	private Deque<OutputLogEntry> log = new LinkedBlockingDeque<OutputLogEntry>();
	
	private BackupOperatorState bs = null;
	private RawData rw = null;

//	public Iterator<BatchTuplePayload> iterator() { 
//		return buff.iterator(); 
//	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#iterator()
	 */
	@Override
	public Iterator<OutputLogEntry> iterator() { 
		return log.iterator(); 
	}

	public Buffer(){
		BackupOperatorState initState = new BackupOperatorState();
		bs = initState;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#size()
	 */
	@Override
	public int size(){
		return log.size();
	} 
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#getBackupState()
	 */
	@Override
	public BackupOperatorState getBackupState(){
		return bs;
	}

//	public void saveStateAndTrim(BackupOperatorState bs){
//		//Save state
//		this.bs = bs;
//		long ts_e = bs.getState().getData_ts();
//		//Trim buffer, eliminating those tuples that are represented by this state
//		trim(ts_e);
//	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#replaceBackupOperatorState(uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState)
	 */
	@Override
	public void replaceBackupOperatorState(BackupOperatorState bs) {
		// In-memory
		long smem = System.currentTimeMillis();
		this.bs = bs;
		long emem = System.currentTimeMillis();
		// On-disk
//		try {
//	    	// Write the object out to a byte array
//	        FileOutputStream fos = new FileOutputStream("tempBackup");
//	        ExtendedObjectOutputStream out = new ExtendedObjectOutputStream(fos);
//	        
//	        out.writeObject(bs);
//	        out.flush();
//	        out.close();
//	    }
//	    catch(IOException e) {
//	    	e.printStackTrace();
//	    }
//	    long enddisk = System.currentTimeMillis();
//	    System.out.println("MEM: "+(emem-smem)+" DISK: "+(enddisk-emem));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#replaceRawData(uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData)
	 */
	@Override
	public void replaceRawData(RawData rw){
		System.out.println("Storing: "+rw.getData().length+" bytes");
		this.rw = rw;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#save(uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload, long, uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker)
	 */
	@Override
	public void save(BatchTuplePayload batch, long outputTs, TimestampTracker inputTs){
		log.add(new OutputLogEntry(outputTs, inputTs, batch));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#trim(long)
	 */
	@Override
	public TimestampTracker trim(long ts)
	{
		// System.out.println("ACK: "+ts);
		TimestampTracker oldest = null;
		boolean matchFirstEntryToRemove = true;
		long startTrim = System.currentTimeMillis();
		Iterator<OutputLogEntry> iter = log.iterator();
		while (iter.hasNext()) {
			OutputLogEntry next = iter.next();
			BatchTuplePayload batch = next.batch;
			long timeStamp = 0;
			timeStamp = next.outputTs; // the newest ts in the entry
			if (timeStamp <= ts) {
				//Detect first entry to remove and store the inputVTs
				if(matchFirstEntryToRemove){
					///\todo{are we iterating from the tail or the head?}
					matchFirstEntryToRemove = false;
					oldest = next.inputVTs;
				}
//				System.out.println("Remove tuple with ts: "+timeStamp);
				iter.remove();
			}
			else {
				break;
			}
		}
//		if(!log.isEmpty()){
//			oldest = log.getFirst().inputVTs;
//		}
		long endTrim = System.currentTimeMillis();
		System.out.println("TOTAL-TRIM: "+(endTrim-startTrim));

        return oldest;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#trim(uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl)
	 */
	@Override
	public TreeMap<Long, BatchTuplePayload> trim(FailureCtrl fctrl)
	{
		/*
		Iterator<OutputLogEntry> iter = log.iterator();
		while (iter.hasNext())
		{
			BatchTuplePayload batch = iter.next().batch;
			batch.trim(fctrl);
			if (batch.size() <= 0) { iter.remove(); }
		}
		*/
		throw new RuntimeException("Logic error.");
		//TODO: This won't work with SEEPs current batching/acking model.
		//throw new RuntimeException("Need to handle (SEEP) batch sending properly here!");
		
	}
	
	public boolean contains(long ts) { throw new RuntimeException("Logic error."); }
	public BatchTuplePayload get(long ts) { throw new RuntimeException("Logic error."); }
	public TreeMap<Long, BatchTuplePayload> get(FailureCtrl fctrl)
	{
		throw new RuntimeException("Logic error.");
	}
	
	///fixme{just for testing, do binary search on structure}
	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.buffer.IBuffer#getInputVTsForOutputTs(long)
	 */
	@Override
	public TimestampTracker getInputVTsForOutputTs(long output_ts){
		for(OutputLogEntry l : log){
			if(l.outputTs == output_ts){
				return l.inputVTs;
			}
		}
		return null;
	}
}
