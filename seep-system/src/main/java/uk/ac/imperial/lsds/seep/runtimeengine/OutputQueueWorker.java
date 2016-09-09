package uk.ac.imperial.lsds.seep.runtimeengine;

public class OutputQueueWorker
{

	final private Logger logger = LoggerFactory.getLogger(OutputQueueWorker.class);
	private final Object lock = new Object(){};

	private final static long DEFAULT_TIMEOUT = 1 * 1000;
	private boolean connected = false;
	private long reconnectCount = -1;
	private final CtrlDataExchanger exchanger = new CtrlDataExchanger();

	public OutputQueueWorker()
	{


	}

	public int reopenEndpoint(Endpoint dest, int prevReconnectCount)
	{
		synchronized(lock)
		{
			if getReconnectCount < 0 
			{
				reconnectCount++; / Notify?
				spawnWorkerThread((SynchronousCommunicationChannel)dest);	
			}
		}

		waitUntilConnected();	
		return getReconnectCount();
	} 

	private void spawnWorkerThread(final SynchronousCommunicationChannel channel)
	{
		new Thread(new Runnable() { 
			public void run()
			{
				channel.reopenDownstreamDataSocket();
				setConnected();		
				while (true)
				{
					CtrlDataTuple ctrlData = exchanger.getCtrlData();
					boolean success = sendCtrlData(ctrlData, channel);
					if (!success)
					{
						setReconnecting();	
						channel.reopenDownstreamDataSocket();
						setConnected();
					}
				}
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

	private void getReconnectCount() { synchronized(lock) { return reconnectCount; } }
	private void setConnected() { synchronized(lock) { connected = true; lock.notifyAll(); } }
	private void setReconnecting() { synchronized(lock) { connected = false; reconnectCount++; exchanger.clearCtrlData(); lock.notifyAll(); } }

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

	public int sendData(DataTuple data, int prevReconnectCount)
	{
		Add data to output log 
		int reconnectCountCopy = getReconnectCount(); 
		if (prevReconnectCount > reconnectCountCopy) { throw new RuntimeException("Logic error"); }
		else if (prevReconnectCount == reconnectCountCopy)
		{
			setDataSync(data);
			reconnectCountCopy = getReconnectCount();
			
			/*
			while (prevReconnectCount == reconnectCountCopy)
			{
				setDataSync(data);
				try
					exchange (timeout)
					reconnectCountCopy = getReconnectCount();
					break;
				catch Exception e { squash but log? }
				reconnectCountCopy = getReconnectCount();
			}
			*/
		}

		/*
			N.B. This does not guarantee the data was sent. It could have simply been
			added to the socket buffer, or it could actually have failed. However,
			this will be picked up the next time we send. Think that is ok, since we could
			have had just added to the socket buffer previously. 
		*/
		return reconnectCountCopy;
	}


	public void sendControl(ControlTuple control)
	{
		exchanger.setCtrlAsync(control);
	}


	private boolean sendCtrlData(CtrlDataTuple ctrlDataTuple, SynchronousCommunicationChannel channel)
	{
		IBuffer buffer = channelRecord.getBuffer();
		DataTuple tuple = ctrlDataTuple.data;	
		if (tuple != null && buffer.contains(tuple.getPayload().timestamp)) 
		{ 
			LOG.info("oq.dupe ts="+tuple.data.getPayload().timestamp+",dsOpId="+dest.getOperatorId());	
			return true; 
		} 
		//Output for this socket
		try
		{
			if (tuple != null)
			{
				//To send tuple
				TuplePayload tp = tuple.getPayload();
				final boolean allowOutOfOrderTuples = owner.getProcessingUnit().getOperator().getOpContext().getMeanderQuery() != null;
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
				LOG.debug("oq.sync sending ts="+tp.timestamp+" for "+channelRecord.getOperatorId()+", current latency="+latency+", oq latency="+oqLatency);
			}
	
			if(channelRecord.getChannelBatchSize() <= 0 || ctrlDataTuple.rctrl != null || ctrlDataTuple.fctrl != null){
				channelRecord.setTick(currentTime);
				BatchTuplePayload msg = channelRecord.getBatch();
				
				//Don't save if it is an empty batch e.g. when we just have ctrl to send.
				if(!GLOBALS.valueFor("reliability").equals("bestEffort") && msg.size() > 0)
				{
					buffer.save(msg, msg.outputTs, owner.getIncomingTT());
				}

				msg.rctrl = ctrlDataTuple.rctrl;
				msg.fctrl = ctrlDataTuple.fctrl;

				try
				{
					k.writeObject(channelRecord.getOutput(), msg);
					//Flush the buffer to the stream
					channelRecord.getOutput().flush();
				}
				catch(KryoException|IllegalArgumentException e)
				{
					LOG.error("Writing batch to "+dest.getOperatorId() + " failed, ts="+ tp.timestamp+", "+e);
					channelRecord.cleanBatch2();
					return false;
				}
				catch(Exception e) { LOG.error("Unexpected exception, should squash and return false: "+e); System.exit(1); }
				
				// Anf finally we reset the batch
	//					channelRecord.cleanBatch(); // RACE CONDITION ??
				channelRecord.cleanBatch2();
			}
		}
		catch(InterruptedException ie){
			LOG.error("-> Dispatcher. While trying to do wait() "+ie.getMessage());
			ie.printStackTrace();
			System.exit(1);	//dokeeffe abort - don't want this any more.
		}
		return true;
	}

	private class CtrlDataExchanger
	{
		public ControlTuple rctrl = null;
		public ControlTuple fctrl = null;
		public DataTuple data = null;

		public void setCtrlAsync(ControlTuple ctrl)
		{
			synchronized(lock)
			{
				if (ctrl.type == ControlTuple.UpDownRctrl) { rctrl = ctrl; }
				else if (ctrl.type == ControlTuple.FailureCtrl) { fctrl = ctrl; }
				else  { throw new RuntimeException("Logic error."); } 
				lock.notifyAll();
			}
		}

		public void setDataSync(DataTuple newData)
		{
			synchronized(lock)
			{
				if (data != null) { throw new RuntimeException("Logic error"); }
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

		public CtrlDataTuple getCtrlData()
		{
			synchronized(lock)
			{
				while (data == null && rctrl == null && fctrl == null)
				{
					try { lock.wait(DEFAULT_TIMEOUT); } catch(InterruptedException e) { logger.debug("getCtrlData wait timed out"); }
				}
				CtrlDataTuple result = new CtrlDataTuple(data, rctrl, fctrl);	
				clearCtrlData();	
				return result;
			}
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
