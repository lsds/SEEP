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
package uk.ac.imperial.lsds.seep.reliable;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class StreamerWorker implements Runnable{
	
	private ArrayBlockingQueue<Object> jobQueue;
	
//	private List<File> toStream;
//	private int filesToStreamSize = 0;
	private Kryo k;
//	private Output newO;
	private int oldOpId;
//	private int newOpId;
	private int keeperOpId;
	private int totalNumberChunks;
	private int currentNumberBatch;
	
	private Output largeOutput;
	
	public void initializeSerialization(){
		k = new Kryo();
		k.register(ControlTuple.class);
		k.register(MemoryChunk.class);
		k.register(StateChunk.class);
		k.register(HashMap.class, new MapSerializer());
		k.register(BackupOperatorState.class);
		k.register(byte[].class);
		k.register(RawData.class);
		k.register(Ack.class);
		k.register(BackupNodeState.class);
		k.register(Resume.class);
		k.register(ScaleOutInfo.class);
		k.register(StateAck.class);
		k.register(ArrayList.class);
		k.register(BackupRI.class);
		k.register(InitNodeState.class);
		k.register(InitOperatorState.class);
		k.register(InitRI.class);
		k.register(InvalidateState.class);
		k.register(ReconfigureConnection.class);
	}
	
	public StreamerWorker(Socket s, ArrayBlockingQueue<Object> jobQueue, int opId, int keeperOpId, int currentNumberBatch, int totalNumberChunks){
		try {
			largeOutput  = new Output(10000);
			largeOutput.setOutputStream(s.getOutputStream());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.jobQueue = jobQueue;
		this.initializeSerialization();
		this.oldOpId = opId;
		this.keeperOpId = keeperOpId;
		System.out.println("CREATED KEEPER: "+keeperOpId);
		this.currentNumberBatch = currentNumberBatch;
		this.totalNumberChunks = totalNumberChunks;
	}	

	@Override
	public void run() {
		int streamedFiles = 0;
		boolean goOn = true;
		while(goOn){
			ArrayList<Object> p = null;
			try {
				p = (ArrayList<Object>)jobQueue.take();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(p.size() == 0){
				System.out.println("END");
				goOn = false;
				continue;
			}
			
			MemoryChunk oldMC = new MemoryChunk(p);
			ControlTuple oldCT = new ControlTuple().makeStateChunk(oldOpId, keeperOpId, currentNumberBatch, totalNumberChunks, oldMC, 0);
			System.out.println(Thread.currentThread().getName()+" STREAM CHUNK (keeper="+keeperOpId+") to "+largeOutput.toString());
			k.writeObject(largeOutput, oldCT);
			largeOutput.flush();
			streamedFiles++;
		}
		System.out.println("##################");
		System.out.println("##################");
		System.out.println("I streamed this files: "+streamedFiles);
		System.out.println("##################");
		System.out.println("##################");
	}

}
