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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class OutputQueue {
	
	final private Logger LOG = LoggerFactory.getLogger(OutputQueue.class);

	// replaySemaphore controls whether it is possible to send or not
	private CoreRE owner = null;
	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	private Kryo k = null;
    private ExecutorService senderThread;
	
	public OutputQueue(CoreRE owner){
		this.owner = owner;
		this.k = initializeKryo();
        senderThread = Executors.newFixedThreadPool(1);
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
			LOG.warn("-> Dispatcher. replaySemaphore was 0, stays equals ");
			replaySemaphore.set(0);
			return;
		}

		LOG.debug("-> replaySemaphore changes from: {}", replaySemaphore.toString());
		replaySemaphore.decrementAndGet();
		LOG.debug("-> replaySemaphore to: {}", replaySemaphore.toString());
		synchronized(this){
			this.notify();
		}
	}
	
	public synchronized void stop() {
		//Stop incoming data, a new thread is replaying
		LOG.debug("-> replaySemaphore from: {}", replaySemaphore.toString());
		replaySemaphore.incrementAndGet();
		LOG.debug("-> replaySemaphore to: {}", replaySemaphore.toString());
	}
	
	
	public synchronized void sendToDownstream(DataTuple tuple, EndPoint dest)  {
                //System.out.println("Hi, I'm: "+Thread.currentThread().getId());
                
		final SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
		Buffer buffer = channelRecord.getBuffer();
		AtomicBoolean replay = channelRecord.getReplay();
		AtomicBoolean stop = channelRecord.getStop();

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
				TuplePayload tp = tuple.getPayload();
				tp.timestamp = System.currentTimeMillis(); // assign local ack
				channelRecord.addDataToBatch(tp);
				long currentTime = System.currentTimeMillis();

				if(channelRecord.getChannelBatchSize() <= 0){

					channelRecord.setTick(currentTime);
					BatchTuplePayload msg = channelRecord.getBatch();
                    ByteBufferOutput kryoOutput = (ByteBufferOutput)channelRecord.getOutput();
					k.writeObject(kryoOutput, msg);
					//Actually since the Kryo Output we use is a ByteBufferOutput without any output stream, so flush() here doesn't cause anything
                    kryoOutput.flush();

                    final byte[] serialisedData = kryoOutput.toBytes();

                    if(senderThread == null){
                        senderThread = Executors.newFixedThreadPool(1);
                    }
                    senderThread.execute(new Runnable() {
                        public void run() {
                            try {
                                OutputStream downStreamOuputStream = channelRecord.getDownStreamOuputStream();
                                downStreamOuputStream.write(serialisedData);
                                downStreamOuputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    // We save the data
					if(GLOBALS.valueFor("eftMechanismEnabled").equals("true")){
						// while taking latency measures, to avoid that sources and sink in same node will be affected by buffer trimming
						if(GLOBALS.valueFor("TTT").equals("TRUE")){
							
						}
						else{
							buffer.save(msg, msg.outputTs, owner.getIncomingTT());
						}
					}
					// Anf finally we reset the batch
//					channelRecord.cleanBatch(); // RACE CONDITION ??

					channelRecord.cleanBatch2();
				}
			}
			//Is there any thread replaying?
                        
			while(replaySemaphore.get() >= 1){
				//If so, wait.
				synchronized(this){
					this.wait();
				}
			}
		}
		catch(InterruptedException ie){
			LOG.error("-> Dispatcher. While trying to do wait() "+ie.getMessage());
			ie.printStackTrace();
		}
	}
	
	public void replay(SynchronousCommunicationChannel oi){
		long a = System.currentTimeMillis();
				while(oi.getSharedIterator().hasNext()){
					BatchTuplePayload batch = oi.getSharedIterator().next().batch;
					Output output = oi.getOutput();
					k.writeObject(output, batch);
					output.flush();
				}
		long b = System.currentTimeMillis() - a;
		System.out.println("Dis.replay: "+b);
	}
	
	public void replayTuples(SynchronousCommunicationChannel cci) {
		Iterator<OutputLogEntry> sharedIterator = cci.getBuffer().iterator();
		Output output = cci.getOutput();
		int bufferSize = cci.getBuffer().size();
		int controlThreshold = (int)(bufferSize)/10;
		int replayed = 0;
//		while(sharedIterator.hasNext()) {
//			BatchTuplePayload dt = sharedIterator.next().batch;
//			synchronized(output){
//				synchronized(k){
//					k.writeObject(output, dt);
//				}
//				output.flush();
//			}
//			replayed++;
//			/// \test {test this functionality. is this necessary?}
//			if((bufferSize-replayed) <= (controlThreshold+1)){
//				break;
//			}
//		}
		//Restablish communication. Set variables and sharedIterator with the current iteration state.
		LOG.debug("-> Recovering connections");
		cci.getReplay().set(true);
		cci.getStop().set(false);
		cci.setSharedIterator(sharedIterator);
		start();
	}
	
}
