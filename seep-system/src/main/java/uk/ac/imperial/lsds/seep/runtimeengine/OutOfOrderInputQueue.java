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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;

public class OutOfOrderInputQueue implements DataStructureI {

	private final static Logger logger = LoggerFactory.getLogger(OutOfOrderInputQueue.class);
	private TreeMap<Long, DataTuple> inputQueue = new TreeMap<>();
	private final int maxInputQueueSize;
	private long lw = -1;
	private Set<Long> acks = new HashSet<>();
	
	public OutOfOrderInputQueue()
	{
		//inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
		maxInputQueueSize = Integer.parseInt(GLOBALS.valueFor("inputQueueLength")); 
		logger.info("Setting max input queue size to:"+ maxInputQueueSize);
	}
	
	public synchronized void push(DataTuple data){
		
		//TODO: Should really check alives too (at least the local ones).
		if (data.getPayload().timestamp <= lw || 
				acks.contains(lw) ||
				inputQueue.containsKey(data.getPayload().timestamp)) 
		{
			logger.debug("Ignoring tuple with ts="+data.getPayload().timestamp);
			return; 
		}
		
		while (inputQueue.size() >= maxInputQueueSize)
		{
			logger.debug("Blocking on full input queue.");
			try {
				this.wait();
			} catch (InterruptedException e) {}
		}
		
		
		inputQueue.put(data.getPayload().timestamp, data);
		logger.debug("Added tuple "+data.getPayload().timestamp+",sz="+inputQueue.size());
		this.notifyAll();
        notifyThat(0).inputQueuePut();
	}
	
	public synchronized boolean pushOrShed(DataTuple data){
		
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
	
	public synchronized DataTuple pull(){
    	while (inputQueue.isEmpty())
    	{
    		try {
				this.wait();
			} catch (InterruptedException e) {}
    	}
        // Seep monitoring
        notifyThat(0).inputQueueTake();
        DataTuple dt = inputQueue.remove(inputQueue.firstKey());
        this.notifyAll();
        return dt;
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
	public synchronized FailureCtrl purge(FailureCtrl nodeFctrl) {
		
		if (nodeFctrl.lw() < lw || (nodeFctrl.lw() == lw && nodeFctrl.acks().size() < acks.size()))
		{
			throw new RuntimeException("Logic error");
		}
		lw = nodeFctrl.lw();
		acks = nodeFctrl.acks();
		
		Set<Long> opAlives = new HashSet<>();
		Iterator<Long> iter = inputQueue.keySet().iterator();
		boolean removedSomething = false;
		while (iter.hasNext())
		{
			long ts = iter.next();
			//TODO: Do we really want to delete alives that aren't local?
			if (ts <= nodeFctrl.lw() || nodeFctrl.acks().contains(ts) 
					|| nodeFctrl.alives().contains(ts))
			{
				iter.remove();
				removedSomething = true;
			}
			else
			{
				opAlives.add(ts);
			}
		}
		FailureCtrl upOpFctrl = new FailureCtrl(nodeFctrl);
		upOpFctrl.updateAlives(opAlives);
		if (removedSomething) { this.notifyAll(); }
		return upOpFctrl;
	}

	@Override
	public synchronized int size() {
		return inputQueue.size();
	}
	
	
}
