/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.imperial.lsds.seep.P;
import uk.co.imperial.lsds.seep.buffer.Buffer;
import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.co.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.co.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.co.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.co.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class OutputQueue {

	// replaySemaphore controls whether it is possible to send or not
	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	private Kryo k = null;
	
	public OutputQueue(){
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		return k;
	}
	
	//Start incoming data, one thread has finished replaying
	public synchronized void start(){
		/// \todo {this is a safe check that should not be done because we eventually will be sure that it works well}
		if(replaySemaphore.get() == 0){
			NodeManager.nLogger.warning("-> Dispatcher. replaySemaphore was 0, stays equals ");
			replaySemaphore.set(0);
			return;
		}

		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore changes from: "+replaySemaphore.toString());
		replaySemaphore.decrementAndGet();
		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore to: "+replaySemaphore.toString());
		synchronized(this){
			this.notify();
		}
	}
	
	public synchronized void stop() {
		//Stop incoming data, a new thread is replaying
		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore changes from: "+replaySemaphore.toString());
		/**
		 * hack done on july the third 2012 to get parallel recovery results.
		 *  we make sure that conn is only stop once
		 */
//if (replaySemaphore.get() > 0){
//	return;
//}
		replaySemaphore.incrementAndGet();
		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore to: "+replaySemaphore.toString());
	}
	
	
	public synchronized void sendToDownstream(DataTuple tuple, EndPoint dest, boolean now, boolean beacon) {
//System.out.println("A");
		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
		
		Buffer buffer = channelRecord.getBuffer();
		AtomicBoolean replay = channelRecord.getReplay();
		AtomicBoolean stop = channelRecord.getStop();
		//Output for this socket
//		Output output = channelRecord.getOutput();
		try{
			//To send tuple
//System.out.println("B");
			if(replay.compareAndSet(true, false)){
//System.out.println("C");
//				System.out.println("WE ARE IN REPLAY OUTPUTQUEUE");
				replay(channelRecord);
				replay.set(false);
				stop.set(false);
//				System.out.println("finished REPLAY OUTPUTQUUE");
				//At this point, this operator has finished replaying the tuples
				NodeManager.setSystemStable();
			}
//System.out.println("D");
			if(!stop.get()){
//System.out.println("E");
//				System.out.println("SEND IN OTUPUTQUEUE");
				if(!beacon){
					channelRecord.addDataToBatch(tuple.getPayload());
				}
				//If it is mandated to send the tuple now (URGENT), then channelBatchSize is put to 0
				if(now) channelRecord.resetChannelBatchSize();
				long currentTime = System.currentTimeMillis();
				/// \todo{Add the following line for include the batch timing mechanism}
//				if(channelRecord.channelBatchSize == 0 || (currentTime - channelRecord.getTick) > ExecutionConfiguration.maxLatencyAllowed ){
				
				/** shouldnt be less than 0 ever... **/
				
//				System.out.println("Before getting to send batch OUTPUTEUQUE");
//System.out.println("F");
				if(channelRecord.getChannelBatchSize() <= 0){
//System.out.println("G");
					channelRecord.setTick(currentTime);
					BatchTuplePayload msg = channelRecord.getBatch();
					k.writeObject(channelRecord.getOutput(), msg);
//System.out.println("H");
					//Flush the buffer to the stream
					channelRecord.getOutput().flush();
					
					// We log the data
					if(P.valueFor("eftMechanismEnabled").equals("true")){
						// while taking latency measures, to avoid that sources and sink in same node will be affected by buffer trimming
						if(P.valueFor("TTT").equals("TRUE")){
							
						}
						else{
							buffer.save(msg);
						}
					}
					// Anf finally we reset the batch
//					channelRecord.cleanBatch(); // RACE CONDITION ??
					channelRecord.cleanBatch2();
				}
			}
			else if (!beacon){
//System.out.println("I");
				//Is there any thread replaying?
				while(replaySemaphore.get() >= 1){
//System.out.println("J");
					//If so, wait.
					synchronized(this){
//System.out.println("K");
						this.wait();
					}
//System.out.println("L");
				}
			}
		}
		catch(InterruptedException ie){
			NodeManager.nLogger.severe("-> Dispatcher. While trying to do wait() "+ie.getMessage());
			ie.printStackTrace();
		}
	}
	
	public void replay(SynchronousCommunicationChannel oi){
		long a = System.currentTimeMillis();
				while(oi.getSharedIterator().hasNext()){
//					BatchDataTuple batch = oi.getSharedIterator().next();
					BatchTuplePayload batch = oi.getSharedIterator().next();
					Output output = oi.getOutput();
					k.writeObject(output, batch);
					output.flush();
				}
		long b = System.currentTimeMillis() - a;
		System.out.println("Dis.replay: "+b);
	}
	
	public void replayTuples(SynchronousCommunicationChannel cci) {
//		Iterator<BatchDataTuple> sharedIterator = cci.getBuffer().iterator();
		Iterator<BatchTuplePayload> sharedIterator = cci.getBuffer().iterator();
		Output output = cci.getOutput();
		int bufferSize = cci.getBuffer().size();
		int controlThreshold = (int)(bufferSize)/10;
		int replayed = 0;
		while(sharedIterator.hasNext()) {
//			BatchDataTuple dt = sharedIterator.next();
			BatchTuplePayload dt = sharedIterator.next();
			synchronized(output){
				synchronized(k){
					k.writeObject(output, dt);
				}
				output.flush();
			}
			replayed++;
			/// \test {test this functionality. is this necessary?}
			if((bufferSize-replayed) <= (controlThreshold+1)){
				break;
			}
		}
		//Restablish communication. Set variables and sharedIterator with the current iteration state.
		NodeManager.nLogger.info("-> Recovering connections");
		cci.getReplay().set(true);
		cci.getStop().set(false);
		cci.setSharedIterator(sharedIterator);
		start();
	}
	
}
