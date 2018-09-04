package uk.ac.imperial.lsds.seep.runtimeengine;

import static uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.DefaultMetricsNotifier.notifyThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.manet.Query;

public class OutOfOrderFairInputQueue implements DataStructureI {

	private final static Logger logger = LoggerFactory.getLogger(OutOfOrderFairInputQueue.class);
	private TreeMap<Long, DataTuple> inputQueue = new TreeMap<>();
	private final int maxInputQueueSize;
	//private long lw = -1;
	//private Set<Long> acks = new HashSet<>();
	private final boolean optimizeReplay;
	private final boolean reprocessNonLocals;
	private final boolean bestEffort;
	private final ReentrantLock lock = new ReentrantLock(true){};
	private final Condition cond = lock.newCondition();
	
	private final FailureCtrl inputFctrl = new FailureCtrl();
	
	public OutOfOrderFairInputQueue(Query frontierQuery, int opId)
	{
		boolean isSink = frontierQuery.isSink(frontierQuery.getLogicalNodeId(opId));
		//inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
		int replicationFactor = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
		boolean replicatedSink = isSink && Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor")) > 0;
		boolean boundedOpQueue = !GLOBALS.valueFor("frontierRouting").equals("backpressure") || 
					Boolean.parseBoolean(GLOBALS.valueFor("boundFrontierRoutingQueues")) ||
					(replicationFactor == 1 && !replicatedSink);
		if (boundedOpQueue)
		{
			maxInputQueueSize = Integer.parseInt(GLOBALS.valueFor("inputQueueLength"));
		}
		else
		{
			maxInputQueueSize = Integer.MAX_VALUE;
		}

		bestEffort = GLOBALS.valueFor("reliability").equals("bestEffort");
		optimizeReplay = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay"));
		reprocessNonLocals = Boolean.parseBoolean(GLOBALS.valueFor("reprocessNonLocals"));
		
		logger.info("Setting max input queue size to:"+ maxInputQueueSize);
		logger.info("Input queue reliability: bestEffort="+bestEffort+",optimizeReplay="+optimizeReplay);
	}
	
	public void push(DataTuple data){
		lock.lock();	
		try
		{	
			long ts = data.getPayload().timestamp; 
			if (!bestEffort && !inputFctrl.updateAlives(ts))
			{
				logger.debug("Ignoring tuple with ts="+ts);
				return; 
			}
			
			while (inputQueue.size() >= maxInputQueueSize)
			{
				logger.debug("Blocking on full input queue.");
				try {
					cond.await();
				} catch (InterruptedException e) {}
			}
			
			
			inputQueue.put(data.getPayload().timestamp, data);
			logger.debug("Added tuple "+data.getPayload().timestamp+",sz="+inputQueue.size());
			cond.signalAll();
			notifyThat(0).inputQueuePut();
		}
		finally { lock.unlock(); }
	}

	public boolean pushOrShed(DataTuple data){
		
		//TODO: Check not acked
		//if tuple <= lw or in acks,
			//return
		//if queue is !map and queue contains tuple
			//return
		
		/*
		boolean inserted = inputQueue.offer(data);
		if (inserted) {
            // Seep monitoring
            notifyThat(0).inputQueuePut();
        }
        
		return inserted;
		*/
		throw new RuntimeException("TODO");
	}
	
	public DataTuple[] pullMiniBatch(){
		/*
		int miniBatchSize = 10;
		DataTuple[] batch = new DataTuple[miniBatchSize];

        // Seep monitoring: notify reset of input queue
        notifyThat(0).inputQueueTake();
         
        synchronized(this)
        {
	        for(int i = 0; i<miniBatchSize; i++){
				DataTuple dt = inputQueue.poll();
				if(dt != null)
					batch[i] = dt;
				else
					break;
			}
        }
		return batch;
		*/
		throw new RuntimeException("TODO");
	}
	
	public  DataTuple pull(){
		lock.lock();	
		try
		{	
			while (inputQueue.isEmpty())
			{
				try {
					cond.await();
				} catch (InterruptedException e) {}
			}
			// Seep monitoring
			notifyThat(0).inputQueueTake();
			DataTuple dt = inputQueue.remove(inputQueue.firstKey());
			cond.signalAll();
			return dt;
		}
		finally { lock.unlock(); }
	}
	
	public void clean(){
		/*
		try {
            // Seep monitoring
            notifyThat(1).inputQueueTake();
        
            inputQueue.take();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BEFORE- REAL SIZE OF INPUT QUEUE: " + inputQueue.size());
		
        // Seep monitoring: notify reset of input queue
        notifyThat(0).inputQueueReset();
        
		inputQueue.clear();
		System.out.println("AFTER- REAL SIZE OF INPUT QUEUE: " + inputQueue.size());
		*/
		throw new RuntimeException("TODO");
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}

	//TODO: Should really allow to distinguish between local alives
	//and downstream alives, since we can avoid adding most of the former 
	// in the first place in push.
	@Override
	public ArrayList<FailureCtrl> purge(FailureCtrl downFctrl) {
		
		lock.lock();
		try
		{
			if (bestEffort || 
					downFctrl.lw() < inputFctrl.lw() || 
					(downFctrl.lw() == inputFctrl.lw() && downFctrl.acks().size() < inputFctrl.acks().size()))
			{
				throw new RuntimeException("Logic error");
			}
			
			//Trim, but don't pollute the record of batches received on this input
			//with the ids of all tuples live downstream.
			//inputFctrl.update(downFctrl.lw(), downFctrl.acks(), null);
			inputFctrl.update(downFctrl, false);
			
			Iterator<Long> iter = inputQueue.keySet().iterator();
			boolean removedSomething = false;
			while (iter.hasNext())
			{
				long ts = iter.next();
				//Probably want to delete tuples that are live downstream but not
				//here to prevent leaks with multi-input ops.
				if (ts <= inputFctrl.lw() || inputFctrl.acks().contains(ts) 
						|| (!reprocessNonLocals && downFctrl.alives().contains(ts)))
				{
					iter.remove();
					removedSomething = true;
				}
			}
			
			FailureCtrl upOpFctrl = null;
			if (optimizeReplay)
			{
				//upOpFctrl = new FailureCtrl(inputFctrl);
				upOpFctrl = inputFctrl.copy();
				upOpFctrl.updateAlives(downFctrl.alives()); 
			}
			else
			{
				//upOpFctrl = new FailureCtrl(downFctrl);
				upOpFctrl = downFctrl.copy();
			}
		
			if (removedSomething) { cond.signalAll(); }
			ArrayList<FailureCtrl> upOpFctrls = new ArrayList<FailureCtrl>(1);
			upOpFctrls.add(upOpFctrl);
			return upOpFctrls;
		}
		finally { lock.unlock(); }

	}

	@Override
	public int size() {
		lock.lock(); 
		try { return inputQueue.size(); }
		finally { lock.unlock(); }
	}
	
	
}
