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
import java.util.concurrent.LinkedBlockingDeque;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.MetricsReader;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

/**
* Buffer class models the buffers for the connections between operators in our system
*/

public class Buffer implements Serializable{

	private static final long serialVersionUID = 1L;

//	private Deque<BatchTuplePayload> buff = new LinkedBlockingDeque<BatchTuplePayload>();
	
	private Deque<OutputLogEntry> log = new LinkedBlockingDeque<OutputLogEntry>();
	
	private BackupOperatorState bs = null;
	private RawData rw = null;

//	public Iterator<BatchTuplePayload> iterator() { 
//		return buff.iterator(); 
//	}
	
	public Iterator<OutputLogEntry> iterator() { 
		return log.iterator(); 
	}

	public Buffer(){
		BackupOperatorState initState = new BackupOperatorState();
		bs = initState;
	}
	
	public int size(){
		return log.size();
	}

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
	
	public void replaceRawData(RawData rw){
		System.out.println("Storing: "+rw.getData().length+" bytes");
		this.rw = rw;
	}

	public void save(BatchTuplePayload batch, long outputTs, TimestampTracker inputTs){
		log.add(new OutputLogEntry(outputTs, inputTs, batch));
		MetricsReader.loggedEvents.inc();
	}
	
/// \test trim() should be tested
/// \todo more efficient way of trimming buffer. -> removeAll(collection to be removed)
	public void trim(long ts){
		long startTrim = System.currentTimeMillis();
		Iterator<OutputLogEntry> iter = log.iterator();
		int numOfTuplesPerBatch = 0;
		while (iter.hasNext()) {
			BatchTuplePayload next = iter.next().batch;
			long timeStamp = 0;
			numOfTuplesPerBatch = next.batchSize;
			//Accessing last index cause that is the newest tuple in the batch

			timeStamp = next.getTuple(numOfTuplesPerBatch-1).timestamp;
			if (timeStamp <= ts) iter.remove();
			else break;
		}
		long endTrim = System.currentTimeMillis();
		MetricsReader.loggedEvents.clear();
		MetricsReader.loggedEvents.inc(log.size());
	}
}
