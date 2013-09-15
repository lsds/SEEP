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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import uk.ac.imperial.lsds.seep.comm.routing.Router;
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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class StreamerWorker implements Runnable{
	
	private ArrayBlockingQueue<Object> jobQueue;
	
	private List<File> toStream;
	private int filesToStreamSize = 0;
	private Kryo k;
	private Output oldO;
	private Output newO;
	private int oldOpId;
	private int newOpId;
	private int keeperOpId;
	private int totalNumberChunks;
	private int currentNumberBatch;
	
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
	
	public StreamerWorker(Output oldO, ArrayBlockingQueue<Object> jobQueue, int opId, int keeperOpId, int currentNumberBatch, 
			int totalNumberChunks){
		this.oldO = oldO;
		this.jobQueue = jobQueue;
		this.initializeSerialization();
		this.oldOpId = opId;
		this.keeperOpId = keeperOpId;
		this.currentNumberBatch = currentNumberBatch;
		this.totalNumberChunks = totalNumberChunks;
	}	
	
	
//	public StreamerWorker(List<File> filesToStreamSplit1, int filesToStreamSize, Socket oldS, Socket newS,
//			int oldOpId, int newOpId, int keeperOpId, int totalNumberChunks){
//		this.toStream = filesToStreamSplit1;
//		this.filesToStreamSize = filesToStreamSize;
//		initializeSerialization();
//		this.oldOpId = oldOpId;
//		this.newOpId = newOpId;
//		this.keeperOpId = keeperOpId;
//		this.totalNumberChunks = totalNumberChunks;
//		try {
//			oldO = new Output(oldS.getOutputStream());
//			newO = new Output(newS.getOutputStream());
//		} 
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
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
			System.out.println(Thread.currentThread().getName()+" STREAM CHUNK to "+oldO.toString());
			k.writeObject(oldO, oldCT);
			oldO.flush();
			streamedFiles++;
		}
		System.out.println("##################");
		System.out.println("##################");
		System.out.println("I streamed this files: "+streamedFiles);
		System.out.println("##################");
		System.out.println("##################");
		
//		// There is a fixed size per chunk, so there is an upper bound size per partition. Let's then
//		// make dynamically-sized chunks.
//		// Every two file chunks, we send the batched state
//		Input i = null;
//		ArrayList<Object> oldPartition = new ArrayList<Object>();
//		ArrayList<Object> newPartition = new ArrayList<Object>();
//		int numberBatchChunks = 2;
//		int currentNumberBatch = 0;
//		for(File chunk : toStream){
//			currentNumberBatch++;
//			try {
//				i = new Input(new FileInputStream(chunk));
//			} 
//			catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ControlTuple ct = k.readObject(i, ControlTuple.class);
//			MemoryChunk mc = ct.getStateChunk().getMemoryChunk();
//			int key = ct.getStateChunk().getSplittingKey(); // read it every time? ...
//			Object sample = mc.chunk.get(0);
//			// agh... java...
//			///\todo{i may bring this info in memoryChunk so that it is not necessary to do that erro-prone sample above...}
//			if(sample instanceof Integer){
//				for(int j = 0; j < mc.chunk.size(); j++){
//					Integer k = (Integer)mc.chunk.get(j);
//					if(Router.customHash(k) > key){
//						newPartition.add(k);
//						j++;
//						newPartition.add(mc.chunk.get(j));
//					}
//					else{
//						oldPartition.add(k);
//						j++;
//						oldPartition.add(mc.chunk.get(j));
//					}
//				}
//			}
//			else if(sample instanceof String){
//				for(int j = 0; j < mc.chunk.size(); j++){
//					String k = (String)mc.chunk.get(j);
//					if(Router.customHash(k) > key){
//						newPartition.add(k);
//						j++;
//						newPartition.add(mc.chunk.get(j));
//					}
//					else{
//						oldPartition.add(k);
//						j++;
//						oldPartition.add(mc.chunk.get(j));
//					}
//				}
//			}
//			if(currentNumberBatch == numberBatchChunks){
//				currentNumberBatch = 0;
//				MemoryChunk oldMC = new MemoryChunk(oldPartition);
//				ControlTuple oldCT = new ControlTuple().makeStateChunk(oldOpId, keeperOpId, currentNumberBatch, totalNumberChunks, oldMC, 0);
//				k.writeObject(oldO, oldCT);
//				oldO.flush();
//				MemoryChunk newMC = new MemoryChunk(newPartition);
//				ControlTuple newCT = new ControlTuple().makeStateChunk(newOpId, keeperOpId, currentNumberBatch, currentNumberBatch, newMC, 0);
//				k.writeObject(newO, newCT);
//				newO.flush();
//				oldPartition.clear();
//				newPartition.clear();
//			}
//			// close stream to file chunk
//			i.close();
		}

}
