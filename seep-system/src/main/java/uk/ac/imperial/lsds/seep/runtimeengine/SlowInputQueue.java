package uk.ac.imperial.lsds.seep.runtimeengine;

import static uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.DefaultMetricsNotifier.notifyThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;

public class SlowInputQueue implements DataStructureI {

	
	private BlockingQueue<DataTuple> inputQueue;
	
	public SlowInputQueue()
	{
		inputQueue = new ArrayBlockingQueue<DataTuple>(Integer.parseInt(GLOBALS.valueFor("inputQueueLength")));
	}
	
	public synchronized void push(DataTuple data){
		try {
			inputQueue.put(data);
            
            // Seep monitoring
            notifyThat(0).inputQueuePut();
            
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean pushOrShed(DataTuple data){
		boolean inserted = inputQueue.offer(data);
		if (inserted) {
            // Seep monitoring
            notifyThat(0).inputQueuePut();
        }
        
		return inserted;
	}
	
	public DataTuple[] pullMiniBatch(){
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
	}
	
	public DataTuple pull(){
		try {
            // Seep monitoring
            notifyThat(0).inputQueueTake();
            
            synchronized(this)
            {
            	return inputQueue.take();
            }
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void clean(){
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
	}

	@Override
	public ArrayList<DataTuple> pull_from_barrier() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean contains(long timestamp, int upstreamOpId) 
	{ 
		//TODO: Tmp hack! I guess this is only a perf opt anyway,
		//so probably wouldn't bother with it as is.
		Object[] tuples = null;
		synchronized(this) { tuples = inputQueue.toArray(); }
		for (int i = 0; i < tuples.length; i++) { 
			if (((DataTuple)tuples[i]).getPayload().timestamp == timestamp) { return true; }
		}
		return false; 
	}

	@Override
	public Set<Long> getTimestamps() {
		Set<Long> result = new HashSet<>();
		Object[] tuples = null;
		synchronized(this) { tuples = inputQueue.toArray(); }
		for (int i = 0; i < tuples.length; i ++)
		{
			result.add(((DataTuple)tuples[i]).getPayload().timestamp);
		}
		return result;
	}

	@Override
	public synchronized FailureCtrl purge(FailureCtrl nodeFctrl) {
		//TODO: This will be much slower than input queue since the data
		//consumer methods must now take the lock
		Set<Long> opAlives = new HashSet<>();
		Iterator<DataTuple> iter = inputQueue.iterator();
		while (iter.hasNext())
		{
			long ts = iter.next().getPayload().timestamp;
			if (ts <= nodeFctrl.lw() || nodeFctrl.acks().contains(ts) 
					|| nodeFctrl.alives().contains(ts))
			{
				iter.remove();
			}
			else
			{
				opAlives.add(ts);
			}
		}
		FailureCtrl upOpFctrl = new FailureCtrl(nodeFctrl);
		upOpFctrl.updateAlives(opAlives);
		return upOpFctrl;
	}
	
	
}
