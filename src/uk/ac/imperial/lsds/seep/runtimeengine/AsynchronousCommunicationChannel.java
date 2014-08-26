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

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.ac.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class AsynchronousCommunicationChannel implements EndPoint{

	private int opId;
	private Buffer buf;
	private Selector s;
	
	//Serialization tools
	private Kryo k;
	private Output o;
	
	//Native buffer
	private ByteBuffer nativeBuffer;
	
	//Batching variables
	private int batchSize = new Integer(GLOBALS.valueFor("batchLimit"));
	int currentBatchSize = 0;
	
	public AsynchronousCommunicationChannel(int opId, Buffer buf, Output o, ByteBuffer nativeBuffer){
		this.opId = opId;
		this.buf = buf;
		this.o = o;
		this.k = this.initializeKryo();
		this.nativeBuffer = nativeBuffer;
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
//		k.register(BatchTuplePayload.class);
		k.setAsmEnabled(true);
		return k;
	}
	
	public void setSelector(Selector s){
		this.s = s;
	}
	
	@Override
	public int getOperatorId() {
		return opId;
	}
	
	public Output getOutput(){
		return o;
	}
	
	public ByteBuffer getNativeBuffer(){
		return nativeBuffer;
	}
	
	public Kryo getKryo(){
		return k;
	}
	
	public void resetBatch(){
		currentBatchSize = 0;
		readyToWrite = false;
	}
	
	boolean readyToWrite = false;
	
	public boolean isBatchAvailable(){
		return readyToWrite;
	}
	
	public void writeDataToOutputBuffer(DataTuple dt){
		///\fixme{Handle timestamps for fault tolerance}
		// This writes the serialized message to the byte[] that output is backing up
//		if(currentBatchSize < batchSize){
//			System.out.println("curren: "+currentBatchSize+" < totallimit: "+batchSize);
			k.writeObject(o, dt.getPayload());

			currentBatchSize++;
			if(currentBatchSize == batchSize){
//				System.out.println("current: "+currentBatchSize+" == batchlimit "+batchSize);

//				System.out.println("o.position: "+ o.position());
				// Write to buffer
				o.flush();
				synchronized(this){
					readyToWrite = true;
					try {
//						System.out.println("Waiting...");
						this.wait();
//						System.out.println("Unblocked!");
						
						// Reset the batch
						currentBatchSize = 0;
//						System.out.println("After flushing");
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
//		}
	}

}

/**
 * 
 public void sendToDownstream(DataTuple tuple, EndPoint dest, boolean now, boolean beacon) {

		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
		Buffer buffer = channelRecord.getBuffer();
		AtomicBoolean replay = channelRecord.getReplay();
		AtomicBoolean stop = channelRecord.getStop();
		//Output for this socket
		Output output = channelRecord.getOutput();
		try{
			//To send tuple
			if(replay.compareAndSet(true, false)){
				replay(channelRecord);
				replay.set(false);
				stop.set(false);
				//At this point, this operator has finished replaying the tuples
				NodeManager.setSystemStable();
			}
			if(!stop.get()){
				if(!beacon){
					channelRecord.addDataToBatch(tuple.getPayload());
				}
				//If it is mandated to send the tuple now (URGENT), then channelBatchSize is put to 0
				if(now) channelRecord.resetChannelBatchSize();
				long currentTime = System.currentTimeMillis();
				/// \todo{Add the following line for include the batch timing mechanism}
//				if(channelRecord.channelBatchSize == 0 || (currentTime - channelRecord.getTick) > ExecutionConfiguration.maxLatencyAllowed ){
				if(channelRecord.getChannelBatchSize() == 0){
//					BatchDataTuple msg = channelRecord.getBatch();
					BatchTuplePayload msg = channelRecord.getBatch();
					channelRecord.setTick(currentTime);
					
					k.writeObject(output, msg);
					//Flush the buffer to the stream
					output.flush();
					channelRecord.cleanBatch();
					
					if(P.valueFor("eftMechanismEnabled").equals("true")){
//							buffer.save(msg);
					}
				}
			}
			else if (!beacon){
				//Is there any thread replaying?
				while(replaySemaphore.get() >= 1){
					//If so, wait.
					synchronized(this){
						this.wait();
					}
				}
			}
		}
		catch(InterruptedException ie){
			NodeManager.nLogger.severe("-> Dispatcher. While trying to do wait() "+ie.getMessage());
			ie.printStackTrace();
		}
	}
 */ 
