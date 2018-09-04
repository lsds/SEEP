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
package uk.ac.imperial.lsds.seep.comm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpFailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureI;
import uk.ac.imperial.lsds.seep.runtimeengine.OutOfOrderBufferedBarrier;
import uk.ac.imperial.lsds.seep.runtimeengine.OutOfOrderFairBufferedBarrier;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;

public class IncomingDataHandlerWorker implements Runnable{

	final private Logger LOG = LoggerFactory.getLogger(IncomingDataHandlerWorker.class);
	
	private Socket upstreamSocket = null;
	private CoreRE owner = null;
	private boolean goOn;
	private Map<String, Integer> idxMapper;
	private DataStructureAdapter dsa;
	private Kryo k = null;
	private Stats stats;
	private final BlockingQueue<ControlTuple> ctrlQueue;
	
	public IncomingDataHandlerWorker(Socket upstreamSocket, CoreRE owner, Map<String, Integer> idxMapper, DataStructureAdapter dsa){
		//upstream id
		this.upstreamSocket = upstreamSocket;
		this.owner = owner;
		this.goOn = true;
		this.idxMapper = idxMapper;
		this.dsa = dsa;
		this.k = initializeKryo();
		InetSocketAddress inSocketAddr = (InetSocketAddress)upstreamSocket.getRemoteSocketAddress();
		this.stats = new Stats(owner.getProcessingUnit().getOperator().getOperatorId(), owner.getOpIdFromInetAddressAndPort(inSocketAddr.getAddress(), inSocketAddr.getPort()));
		this.ctrlQueue = null;
		LOG.info("Created icdhw with ctrlQueue = "+ctrlQueue);
	}

	public IncomingDataHandlerWorker(Socket upstreamSocket, CoreRE owner, Map<String, Integer> idxMapper, DataStructureAdapter dsa, BlockingQueue<ControlTuple> ctrlQueue){
		//upstream id
		this.upstreamSocket = upstreamSocket;
		this.owner = owner;
		this.goOn = true;
		this.idxMapper = idxMapper;
		this.dsa = dsa;
		this.k = initializeKryo();
		InetSocketAddress inSocketAddr = (InetSocketAddress)upstreamSocket.getRemoteSocketAddress();
		this.stats = new Stats(owner.getProcessingUnit().getOperator().getOperatorId(), owner.getOpIdFromInetAddressAndPort(inSocketAddr.getAddress(), inSocketAddr.getPort()));
		this.ctrlQueue = ctrlQueue;
		LOG.info("Created icdhw with ctrlQueue = "+ctrlQueue);
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.setClassLoader(owner.getRuntimeClassLoader());
		
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);

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
		//k.register(ArrayList.class);
		k.register(BackupRI.class);
		k.register(InitNodeState.class);
		k.register(InitOperatorState.class);
		k.register(InitRI.class);
		k.register(InvalidateState.class);
		k.register(ReconfigureConnection.class);
		//k.register(BitSet.class);
		k.register(BitSet.class, new BitSetSerializer());
		k.register(OpFailureCtrl.class);
		k.register(FailureCtrl.class);
		k.register(UpDownRCtrl.class);
		k.register(DownUpRCtrl.class);
		return k;
	}
	
	public void run() {
		
		/** Experimental asyn **/
//		try{
//			//Get inputQueue from owner
//			DataStructureAdapter dsa = owner.getDSA();
//			//Get inputStream of incoming connection
//			InputStream is = upstreamSocket.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is, 8192);
//			Input i = new Input(bis);
//
//			while(goOn){
//				TuplePayload tp = k.readObject(i, TuplePayload.class);
//				DataTuple reg = new DataTuple(idxMapper, tp);
//				dsa.push(reg);
//			}
//		}
//		catch(IOException io){
//			NodeManager.nLogger.severe("-> IncDataHandlerWorker. IO Error "+io.getMessage());
//			io.printStackTrace();
//		}
		

		
		/** experimental sync **/
		try{
			// Get incomingOp id
			//int opId = owner.getOpIdFromInetAddress(((InetSocketAddress)upstreamSocket.getRemoteSocketAddress()).getAddress());
			InetSocketAddress inSocketAddr = (InetSocketAddress)upstreamSocket.getRemoteSocketAddress();
			int opId = owner.getOpIdFromInetAddressAndPort(inSocketAddr.getAddress(), inSocketAddr.getPort());

			int originalOpId = owner.getOriginalUpstreamFromOpId(opId);
			
			DataStructureI dso = null;
			if(dsa.getUniqueDso() != null){
				dso = dsa.getUniqueDso();
				LOG.info("-> Unique data adapter in this node: "+dso);
			}
			else{
				dso = dsa.getDataStructureIForOp(originalOpId);
				LOG.info("-> Multiple data adapters in this node");
			}
			//Get inputStream of incoming connection
			InputStream is = upstreamSocket.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			Input in = new Input(bis);
			BatchTuplePayload batchTuplePayload = null;

			long lastIncomingTs = -1;
			final boolean allowOutOfOrderTuples = owner.getProcessingUnit().getOperator().getOpContext().getFrontierQuery() != null;
			
			while(goOn){
				long receiveStartTs = System.currentTimeMillis();
				batchTuplePayload = k.readObject(in, BatchTuplePayload.class);
				long receiveTs = System.currentTimeMillis();
				long readTime = (receiveTs-receiveStartTs);
				LOG.debug("Received new batch from "+opId+ ",btpayload="+ batchTuplePayload+",readTime="+readTime);
				ArrayList<TuplePayload> batch = batchTuplePayload.batch;
				for(TuplePayload t_payload : batch)
				{
					
					if (!allowOutOfOrderTuples)
					{
						long incomingTs = t_payload.timestamp;
						// Check for already processed data
						/// \todo{should be <= but the problem is that logical clock in java has ms granularity. This means that once you
						/// send more than 1000 events per second, some events are discarded here, since their ts is the same...}
						if(incomingTs < lastIncomingTs){
							System.out.println("Duplicate");
							continue;
						}
						owner.setTsData(opId, incomingTs);
						lastIncomingTs = incomingTs;
					}
					
					//Put data in inputQueue
					if(owner.checkSystemStatus()){
						long latency = receiveTs - t_payload.instrumentation_ts;
						long socketLatency = receiveTs - t_payload.local_ts;
						t_payload.local_ts = receiveTs;
						LOG.debug("icdhw for "+opId+",ts="+t_payload.timestamp+",its="+t_payload.instrumentation_ts+",rx latency="+latency+", socket latency="+socketLatency+", readTime="+readTime);
						DataTuple reg = new DataTuple(idxMapper, t_payload);
						if (reg.getMap().containsKey("value"))
						{
							int length = reg.getValue("value") instanceof String ?  reg.getPayload().toString().length() : reg.getByteArray("value").length;
							Stats.IntervalTput tput = stats.add(System.currentTimeMillis(), length);

							if (tput != null && owner.getRoutingController() != null) { owner.getRoutingController().handleIntervalTputUpdate(tput); } 
						}
						if (reg.getMap().containsKey("latencyBreakdown"))
						{
							long[] latencies = reg.getLongArray("latencyBreakdown");
							long[] newLatencies = new long[latencies.length+2];
							for (int i=0; i < latencies.length; i++) { newLatencies[i] = latencies[i]; }
							newLatencies[latencies.length] = socketLatency;
							newLatencies[latencies.length+1] = readTime;
							reg.getPayload().attrValues.set(reg.getMap().get("latencyBreakdown"), newLatencies);
						}

						LOG.debug("Adding batch to dso, local latency="+(System.currentTimeMillis()-receiveTs));
						if (dso instanceof OutOfOrderBufferedBarrier)
						{
							LOG.debug("Pushing to ooo buffered barrier.");
							((OutOfOrderBufferedBarrier)dso).push(reg, opId);
						}
						else if (dso instanceof OutOfOrderFairBufferedBarrier)
						{
							LOG.debug("Pushing to ooo buffered barrier.");
							((OutOfOrderFairBufferedBarrier)dso).push(reg, opId);
						}
						else
						{
							LOG.debug("Pushing to dso.");
							dso.push(reg);
						}
						LOG.debug("Finished pushing to dso, ts="+t_payload.timestamp+", local latency="+(System.currentTimeMillis()-receiveTs));
					}
					else{
						///\todo{check for garbage in the tcp buffers}
						LOG.warn("Discarding batch as system status not normal.");
					}
				}
				LOG.debug("ichw rctrl="+batchTuplePayload.rctrl + ", fctrl="+batchTuplePayload.fctrl);				
				if (batchTuplePayload.rctrl != null) 
				{ 
					ControlTuple ct = new ControlTuple(ControlTupleType.UP_DOWN_RCTRL, opId, batchTuplePayload.rctrl.intValue());
					ctrlQueue.offer(ct); }
				if (batchTuplePayload.fctrl != null) 
				{ ctrlQueue.offer(batchTuplePayload.fctrl); }
			}
			LOG.error("-> Data connection closing...");
			upstreamSocket.close();
		}
		catch(IOException io){
			LOG.error("-> IncDataHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}
