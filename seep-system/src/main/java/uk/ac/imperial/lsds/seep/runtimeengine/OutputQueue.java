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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

public class OutputQueue {
	
	final private Logger LOG = LoggerFactory.getLogger(OutputQueue.class);

	// replaySemaphore controls whether it is possible to send or not
	private CoreRE owner = null;
	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	private Kryo k = null;
	private final boolean bestEffort;
	private final boolean outputQueueTimestamps;
	private final OutputQueueWorker oqWorker;
	private static final boolean piggybackControlTraffic = Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic"));
	private final boolean enableTupleTracking = Boolean.parseBoolean(GLOBALS.valueFor("enableTupleTracking"));
	private long currentReconnectCount = -1;
	private int opId;
	
	public OutputQueue(CoreRE owner){
		this.owner = owner;
		this.k = initializeKryo();
		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		boolean isSource = owner.getProcessingUnit().getOperator().getOpContext().isSource();
		outputQueueTimestamps = isSource && Boolean.parseBoolean(GLOBALS.valueFor("srcOutputQueueTimestamps"));
		opId = owner.getProcessingUnit().getOperator().getOperatorId();
		if (piggybackControlTraffic) { oqWorker = new OutputQueueWorker(owner, outputQueueTimestamps); } 
		else { oqWorker = null; }
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
	
	public synchronized void reopenEndpoint(EndPoint dest)
	{
		if (piggybackControlTraffic)
		{
			long prevReconnectCount = currentReconnectCount;
			currentReconnectCount = oqWorker.reopenEndpoint(dest, currentReconnectCount);
			LOG.debug("Oq worker reopened endpoint "+dest.getOperatorId() +" with reconnect count = "+currentReconnectCount+", prev="+prevReconnectCount);
		}	
		else
		{
			SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
			channelRecord.reopenDownstreamDataSocket();
		}
	}

	public synchronized boolean checkEndpoint(EndPoint dest)
	{
		if (piggybackControlTraffic) { return oqWorker.isConnected(); }
		else { throw new RuntimeException("TODO"); }
	}

	private synchronized boolean sendToDownstreamPiggybacked(DataTuple tuple, EndPoint dest)
	{
		long prevReconnectCount = currentReconnectCount;
		currentReconnectCount = oqWorker.sendData(tuple, dest, currentReconnectCount);
		LOG.debug("Sent ts="+tuple.getPayload().timestamp+" to downstream "+dest.getOperatorId()+" piggybacked, current="+currentReconnectCount+",prev="+prevReconnectCount);
		return currentReconnectCount == prevReconnectCount;
	}

	//Assumes oqWorker handles synchronization. 
	public boolean sendToDownstream(ControlTuple tuple)
	{
		if (!piggybackControlTraffic) { throw new RuntimeException("Logic error."); }
		return oqWorker.sendControl(tuple);
	}
	
	public synchronized boolean sendToDownstream(DataTuple tuple, EndPoint dest) {
		if (piggybackControlTraffic) { return sendToDownstreamPiggybacked(tuple, dest); }

		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
		
		IBuffer buffer = channelRecord.getBuffer();
		
		if (buffer.contains(tuple.getPayload().timestamp)) 
		{ 
			LOG.info("oq.dupe ts="+tuple.getPayload().timestamp+",dsOpId="+dest.getOperatorId());	
			return true; 
		} 
		AtomicBoolean replay = channelRecord.getReplay();
		AtomicBoolean stop = channelRecord.getStop();
		//Output for this socket
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
				final boolean allowOutOfOrderTuples = owner.getProcessingUnit().getOperator().getOpContext().getFrontierQuery() != null;
				if (!allowOutOfOrderTuples)
				{
					tp.timestamp = System.currentTimeMillis(); // assign local ack
				}
				long currentTime = System.currentTimeMillis();
				if (outputQueueTimestamps) { tp.instrumentation_ts = currentTime; }
				long latency = currentTime - tp.instrumentation_ts;
				long oqLatency = currentTime - tp.local_ts;
				tp.local_ts = currentTime;

				if (tuple.getMap().containsKey("latencyBreakdown"))
				{
					long[] latencies = tuple.getLongArray("latencyBreakdown");
					long[] newLatencies = new long[latencies.length+1];
					for (int i=0; i < latencies.length; i++) { newLatencies[i] = latencies[i]; }
					newLatencies[latencies.length] = oqLatency;
					tuple.getPayload().attrValues.set(tuple.getMap().get("latencyBreakdown"), newLatencies);
				}

				channelRecord.addDataToBatch(tp);
				
				String logline = "t="+System.currentTimeMillis()+", oq.sync "+opId+" sending ts="+tp.timestamp+" for "+channelRecord.getOperatorId()+", current latency="+latency+", oq latency="+oqLatency;
				if (enableTupleTracking) { LOG.info(logline); } else { LOG.debug(logline);}
				if(channelRecord.getChannelBatchSize() <= 0){
					channelRecord.setTick(currentTime);
					BatchTuplePayload msg = channelRecord.getBatch();
					
					// We save the data
					if(GLOBALS.valueFor("eftMechanismEnabled").equals("true")){
						// while taking latency measures, to avoid that sources and sink in same node will be affected by buffer trimming
						if(GLOBALS.valueFor("TTT").equals("TRUE") || 
								GLOBALS.valueFor("reliability").equals("bestEffort") ||
								GLOBALS.valueFor("noBufferSave").equals("true")){
							
						}
						else{
							buffer.save(msg, msg.outputTs, owner.getIncomingTT());
						}
					}

					boolean flushed = false;
					while(!flushed)
					{
						try
						{
							LOG.debug("Writing batch tuple to "+dest.getOperatorId()+", msg="+msg);
							k.writeObject(channelRecord.getOutput(), msg);
							//Flush the buffer to the stream
							channelRecord.getOutput().flush();
							flushed = true;
						}
						catch(KryoException|IllegalArgumentException e)
						{
							LOG.error("Writing batch to "+dest.getOperatorId() + " failed, ts="+ tp.timestamp+", "+e);
							channelRecord.cleanBatch2();
							return false;
						}
						catch(Exception e) { LOG.error("Unexpected exception, should squash and return false: "+e); System.exit(1); }
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
			System.exit(1);	//dokeeffe abort - don't want this any more.
		}
		return true;
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
