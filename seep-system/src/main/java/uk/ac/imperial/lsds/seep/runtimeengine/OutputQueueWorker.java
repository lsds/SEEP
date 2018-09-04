package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;

import uk.ac.imperial.lsds.seep.buffer.IBuffer;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
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
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.manet.Query;

import uk.ac.imperial.lsds.seep.operator.EndPoint;

import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;

public class OutputQueueWorker
{

	final private Logger logger = LoggerFactory.getLogger(OutputQueueWorker.class);
	private final Object lock = new Object(){};

	private final boolean enableUpstreamRoutingCtrl = Boolean.parseBoolean(GLOBALS.valueFor("enableUpstreamRoutingControl"));
	private final boolean mergeFailureAndRoutingCtrl;
	private final boolean enableTupleTracking = Boolean.parseBoolean(GLOBALS.valueFor("enableTupleTracking"));
	private final boolean downIsMultiInput;
	private final static long DEFAULT_TIMEOUT = 1 * 1000;
	private boolean connected = false;
	private long reconnectCount = -1;
	private final CtrlDataExchanger exchanger = new CtrlDataExchanger();
	private final CoreRE owner;
	private final Kryo k;
	private final boolean outputQueueTimestamps;
	private double totalSent = 0;
	private double coalesced = 0;
	private int opId = 0;

	public OutputQueueWorker(CoreRE owner, boolean outputQueueTimestamps)
	{

		/*
		 * N.B. Temporarily disable this since we now close piggybacked control socket at downstream. Still a problem though if disconnected completely, since the upstream probably won't notice until some tcp
		 * timeout is reached (or maybe not even then since it might never receive the tcp close messages if there is a network failure. Actually, it will never even attempt to reconnect, meaning that no further
		 * data or ctrl will be exchanged!
		if (enableUpstreamRoutingCtrl) 
		{ throw new RuntimeException("TODO: Need to fix bug whereby connection hangs because upstream connection has failed due to connection timeout, but ds connection never fails because no data is ever sent on it (because rctrl has timed out) and no non-coalesced ctrl is ever sent on it (because upstream routing ctrl). Solution is to close connection on rctrl timeout at upstream or close both connections at downstream if upstream direction fails. Actually, not even sure the latter will work - will tcp keep trying to close a connection indefinitely? Alternative solution is to ensure some periodic ping is sent ds even with upstream routing ctrl."); }

		*/
		this.owner = owner;
		this.k = initialiseKryo(); 
		this.outputQueueTimestamps = outputQueueTimestamps;
		Query frontierQuery = owner.getProcessingUnit().getOperator().getOpContext().getFrontierQuery();
		opId = owner.getProcessingUnit().getOperator().getOperatorId();
		int logicalId = frontierQuery.getLogicalNodeId(opId);
		boolean opIsMultiInput = frontierQuery.isJoin(logicalId);
		this.downIsMultiInput = !owner.getProcessingUnit().getOperator().getOpContext().isSink() && frontierQuery.isJoin(frontierQuery.getNextHopLogicalNodeId(logicalId));

		int replicationFactor = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
		boolean bpRouting = GLOBALS.valueFor("enableFrontierRouting").equals("true") && GLOBALS.valueFor("frontierRouting").equals("backpressure");
		boolean noRoutingCtrlMessagesToSend = !bpRouting || replicationFactor <= 1 || (!downIsMultiInput && enableUpstreamRoutingCtrl);
		mergeFailureAndRoutingCtrl = Boolean.parseBoolean(GLOBALS.valueFor("mergeFailureAndRoutingCtrl")) && !noRoutingCtrlMessagesToSend;
		logger.info("OutputQueue worker merging failure and routing ctrl? "+mergeFailureAndRoutingCtrl);
	}
	
	private Kryo initialiseKryo()
	{
		Kryo k = new Kryo();
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

	public long reopenEndpoint(EndPoint dest, long prevReconnectCount)
	{
		synchronized(lock)
		{
			if (getReconnectCount() < 0)
			{
				reconnectCount++; // TODO: Notify?
				spawnWorkerThread(dest);	
			}
		}

		waitUntilConnected();	
		return getReconnectCount();
	} 

	private void spawnWorkerThread(final EndPoint dest)
	{
		new Thread(new Runnable() { 
			public void run()
			{
				try
				{
					SynchronousCommunicationChannel channel = (SynchronousCommunicationChannel)dest;
					channel.reopenDownstreamDataSocket();
					owner.getControlHandler().newIncomingConn(channel.getDownstreamDataSocket());
					setConnected();		
					logger.debug("Op "+opId + " connected to downstream: "+dest.getOperatorId());
					while (true)
					{
						CtrlDataTuple ctrlData = exchanger.getCtrlData(channel);
						logger.debug("Op "+opId + " exchanged ctrlData for "+dest.getOperatorId());
						boolean success = sendCtrlData(ctrlData, dest);
						logger.debug("Op "+opId + " sent ctrlData for "+dest.getOperatorId()+", success="+success);
						if (!success)
						{
							setReconnecting();	
							channel.reopenDownstreamDataSocket();
							owner.getControlHandler().newIncomingConn(channel.getDownstreamDataSocket());
							setConnected();
							logger.debug("Op "+opId + " connected to downstream: "+dest.getOperatorId());
						}
					}
				}
				catch(Exception e) { logger.error("Fatal error: "+e); System.exit(1); }
			}
		}).start();
	} 

	private void waitUntilConnected()
	{
		synchronized(lock)
		{
			if (connected) { return; }
			else
			{
				while(!connected)
				{
					try { lock.wait(DEFAULT_TIMEOUT); } 
					catch (Exception e) { logger.debug("waitUntilConnected wait timed out"); }
				}
			}
		}
	}

	private long getReconnectCount() { synchronized(lock) { return reconnectCount; } }
	private void setConnected() { synchronized(lock) { connected = true; lock.notifyAll(); } }
	public boolean isConnected() { synchronized(lock) { return connected; } }
	private void setReconnecting() 
	{ 
		synchronized(lock) 
		{ 
			connected = false; 
			reconnectCount++; 
			exchanger.clearCtrlData(); 
			exchanger.prevRCtrl = null;			
			lock.notifyAll(); 
		} 
	}

	/*
	This is the one that still needs a bit of work. What happens
	when the worker thread is sending a tuple but fails. Does it just
	reconnect and then take the next ctrl data tuple? What if the dispatcher
	thread was already blocked waiting? It's not really safe for it to drop 
	data after reconnecting since it doesn't know whether the sender has added
	it subsequently. I suppose as part of its reconnect it could call clearCtrlData async?
	In which case the sendData call will always just call setDataSync once and then return
	the currentReconnectCount. Is there any chance of a race where you are reconnecting because
	of a ctrl tuple, and then the data sender tries to exchange after you've cleared? I guess so,
	although I think what will happen is you will send the new data tuple and it will then return
	a different connection count, which will cause a subsequent readding etc. I guess what I'm worried
	about is a thread hanging unneccessarily.
	*/

	public long sendData(DataTuple data, EndPoint dest, long prevReconnectCount)
	{
		boolean dupe = logTuple(data.getPayload(), dest);
		long reconnectCountCopy = getReconnectCount(); 
		if (prevReconnectCount > reconnectCountCopy) { throw new RuntimeException("Logic error"); }
		else if (prevReconnectCount == reconnectCountCopy)
		{
			if (!dupe)
			{
				exchanger.setDataSync(data);
				reconnectCountCopy = getReconnectCount();
			}
		}

		/*
			N.B. This does not guarantee the data was sent. It could have simply been
			added to the socket buffer, or it could actually have failed. However,
			this will be picked up the next time we send. Think that is ok, since we could
			have had just added to the socket buffer previously. 
		*/
		return reconnectCountCopy;
	}

	private boolean logTuple(TuplePayload tp, EndPoint dest)
	{
		BatchTuplePayload msg = new BatchTuplePayload();
		msg.addTuple(tp);
		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel)dest;		
		if (channelRecord.getBuffer().contains(tp.timestamp)) 
		{ 
			logger.info("oq.dupe ts="+tp.timestamp+",dsOpId="+dest.getOperatorId());	
			return true; 
		} 
		channelRecord.getBuffer().save(msg, msg.outputTs, owner.getIncomingTT());
		return false;
	}

	public boolean sendControl(ControlTuple control)
	{
		return exchanger.setCtrlAsync(control);
	}


	private boolean sendCtrlData(CtrlDataTuple ctrlDataTuple, EndPoint dest)
	{
		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel)dest;
		IBuffer buffer = channelRecord.getBuffer();
		DataTuple tuple = ctrlDataTuple.data;	
		/*
		if (tuple != null && buffer.contains(tuple.getPayload().timestamp)) 
		{ 
			logger.info("oq.dupe ts="+tuple.getPayload().timestamp+",dsOpId="+dest.getOperatorId());	
			return true; 
		} 
		*/
		//Output for this socket
		if (tuple != null)
		{
			//To send tuple
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
			if (enableTupleTracking) { logger.info(logline); } else { logger.debug(logline);}
		}

		//if(channelRecord.getChannelBatchSize() <= 0 || ctrlDataTuple.rctrl != null || ctrlDataTuple.fctrl != null){
		if(channelRecord.getChannelBatchSize() <= 0 || ctrlDataTuple.rctrl != null || ctrlDataTuple.fctrl != null ||
			(enableUpstreamRoutingCtrl && !downIsMultiInput && !channelRecord.getDownstreamDataSocket().isClosed()) ) {	//last clause is essentially a ping.
			//channelRecord.setTick(currentTime);
			BatchTuplePayload msg = channelRecord.getBatch();
			
			//Don't save if it is an empty batch e.g. when we just have ctrl to send.
			/*
			if(!GLOBALS.valueFor("reliability").equals("bestEffort") && msg.size() > 0)
			{
				buffer.save(msg, msg.outputTs, owner.getIncomingTT());
			}
			*/

			if (ctrlDataTuple.rctrl != null) { msg.rctrl = ctrlDataTuple.rctrl.getUpDown().getQlen(); }
			msg.fctrl = ctrlDataTuple.fctrl;
			totalSent++;
			if (!msg.batch.isEmpty() && (msg.rctrl != null || msg.fctrl != null))
			{ logger.debug("Coalesced data with ctrl traffic, coalseced %="+ (++coalesced/totalSent)); }
			else { logger.debug("No coalescing for "+channelRecord.getOperatorId()+": "+ (coalesced / totalSent) ); }  

			try
			{
				k.writeObject(channelRecord.getOutput(), msg);
				//Flush the buffer to the stream
				channelRecord.getOutput().flush();
			}
			catch(KryoException|IllegalArgumentException e)
			{
				long ts = tuple == null ? -1 : tuple.getPayload().timestamp;
				logger.error("Writing batch to "+dest.getOperatorId() + " failed, ts="+ ts +", "+e);
				channelRecord.cleanBatch2();
				return false;
			}
			catch(Exception e) { logger.error("Unexpected exception, should squash and return false: "+e); System.exit(1); }
			
			// Anf finally we reset the batch
//					channelRecord.cleanBatch(); // RACE CONDITION ??
			channelRecord.cleanBatch2();
		}
		else if (enableUpstreamRoutingCtrl && !downIsMultiInput && channelRecord.getDownstreamDataSocket().isClosed())
		{
			return false;
		} 

		return true;
	}

	private class CtrlDataExchanger
	{
		public ControlTuple rctrl = null;
		public ControlTuple prevRCtrl = null;
		public ControlTuple fctrl = null;
		//public ControlTuple prevFCtrl = null;
		public DataTuple data = null;

		public boolean setCtrlAsync(ControlTuple ctrl)
		{
			synchronized(lock)
			{
				if (isConnected())
				{
					if (ctrl.getType().equals(CoreRE.ControlTupleType.UP_DOWN_RCTRL)) { rctrl = ctrl; }
					else if (ctrl.getType().equals(CoreRE.ControlTupleType.FAILURE_CTRL)) { fctrl = ctrl; }
					else  { throw new RuntimeException("Logic error."); } 
					lock.notifyAll();
					return true;
				}
				else { return false; }
			}
		}

		public void setDataSync(DataTuple newData)
		{
			synchronized(lock)
			{
				if (data != null) { throw new RuntimeException("Logic error"); }
				logger.debug("Setting data with ts="+newData.getPayload().timestamp);
				data = newData;
				lock.notifyAll();
			}

			synchronized(lock)
			{
				while (data != null)
				{
					try { lock.wait(DEFAULT_TIMEOUT); } catch(InterruptedException e) { logger.debug("setDataSync wait timed out"); }
				}	
			}
		}

		public CtrlDataTuple getCtrlData(SynchronousCommunicationChannel channel)
		{
			long waitStart = System.currentTimeMillis();
			synchronized(lock)
			{
				//while (!ctrlDataChanged(channel))
				while (!ctrlDataChanged(channel) && (!enableUpstreamRoutingCtrl || downIsMultiInput || (System.currentTimeMillis() - waitStart < 2*DEFAULT_TIMEOUT)))
				{
					try { lock.wait(DEFAULT_TIMEOUT); } catch(InterruptedException e) { logger.debug("getCtrlData wait timed out"); }
				}
				CtrlDataTuple result = new CtrlDataTuple(data, rctrl, fctrl);	
				if (rctrl != null) { prevRCtrl = rctrl; }	
				clearCtrlData();	
				return result;
			}
		}

		//Assumes lock held.
		private boolean ctrlDataChanged(SynchronousCommunicationChannel channel)
		{
			//TODO: Think this is potentially broken - if the socket is closed and rctrl is null with ds rctrl?
			return data != null || 
				//(rctrl != null && (prevRCtrl == null || (rctrl.getUpDown().getQlen() != prevRCtrl.getUpDown().getQlen() || channel.getDownstreamDataSocket().isClosed()))) || 
				(rctrl != null) || 
				(!mergeFailureAndRoutingCtrl && fctrl != null) ||
				(enableUpstreamRoutingCtrl && !downIsMultiInput && channel.getDownstreamDataSocket().isClosed());

		}

		public void clearCtrlData()
		{
			synchronized(lock)
			{
				data = null;
				rctrl = null;
				fctrl = null;
				lock.notifyAll();
			}
		}
	}

	public static class CtrlDataTuple
	{
		public DataTuple data;
		public ControlTuple rctrl;
		public ControlTuple fctrl;
		public CtrlDataTuple(DataTuple data, ControlTuple rctrl, ControlTuple fctrl)
		{
			this.data = data;
			this.rctrl = rctrl;
			this.fctrl = fctrl;
		}
	}
}
