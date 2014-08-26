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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.io.Output;

/**
* OutputInformation. This class models the information associated to a downstream or upstream connection
*/
public class SynchronousCommunicationChannel implements EndPoint{

	private int targetOperatorId;
	private Socket downstreamDataSocket;
	private Socket downstreamControlSocket;
	private Buffer buffer;
	
	private Output output = null;
	private OutputStream bos = null;
	
	//Set atomic variables to their initial value
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean replay = new AtomicBoolean(false);
	
	private TimestampTracker reconf_ts;
	private long last_ts;
	private Iterator<OutputLogEntry> sharedIterator;
	
	//Batch information for this channel
	private BatchTuplePayload batch = new BatchTuplePayload();
	private int channelBatchSize = Integer.parseInt(GLOBALS.valueFor("batchLimit"));
	private long tick = 0;

	public SynchronousCommunicationChannel(int opId, Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.targetOperatorId = opId;
		this.downstreamDataSocket = downstreamSocketD;
		this.downstreamControlSocket = downstreamSocketC;
		this.buffer = buffer;
		try {
			/// \fixme{this must be fixed, different CONSTRUCTORS, please...}
			if(downstreamDataSocket != null){
				//Create buffered output stream
				//Create common outputstream and let kryo to manage buffers
				bos = downstreamSocketD.getOutputStream();
				//Create the kryo output for this socket
				output = new Output(bos);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getOperatorId(){
		return targetOperatorId;
	}
	
	public Socket getDownstreamControlSocket(){
		return downstreamControlSocket;
	}
	

	public void setSharedIterator(Iterator<OutputLogEntry> i){
		this.sharedIterator = i;
	}
	
	public Iterator<OutputLogEntry> getSharedIterator(){
		return sharedIterator;
	}
	
	public Output getOutput() {
		return output;
	}
	
	public void setTick(long tick){
		this.tick = tick;
	}
	
	public Socket getDownstreamDataSocket(){
		return downstreamDataSocket;
	}
	
	public Buffer getBuffer(){
		return buffer;
	}
	
	public AtomicBoolean getReplay(){
		return replay;
	}
	
	public AtomicBoolean getStop(){
		return stop;
	}
	
	public synchronized BatchTuplePayload getBatch(){
		return batch;
	}
	
	public synchronized void addDataToBatch(TuplePayload payload){
		batch.addTuple(payload);
		channelBatchSize--;
		last_ts = payload.timestamp;
	}
	
	public int getChannelBatchSize(){
		return channelBatchSize;
	}
	
	public void resetChannelBatchSize(){
		channelBatchSize = 0;
	}
	
	public void cleanBatch(){
		batch.clear();
		int limit = Integer.parseInt(GLOBALS.valueFor("batchLimit"));
		channelBatchSize = limit;
	}
	
	public void cleanBatch2(){
		batch = new BatchTuplePayload();
	}
	
	public long getLast_ts(){
		return last_ts;
	}
	
	public TimestampTracker getReconf_ts(){
		return reconf_ts;
	}
	
	public void setReconf_ts(TimestampTracker ts){
		this.reconf_ts = ts;
	}
}
