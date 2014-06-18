package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.micro.MicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.WindowAPI;

public class SubQuery {
	
	Map<Integer, BlockingQueue<DataTuple>> inputQueues;
	
	Map<Integer, BlockingQueue<DataTuple>> outputQueues;
	
	Set<IMicroOperatorConnectable> microOperators;
	
	private int id;
	
	ISubQueryConnectable parent;
	
	public class SubQueryWorker implements Runnable {
		
		private int inQueueIndex = 0;
		private int outQueueIndex = 0;
		private int batchSize;
		
		public SubQueryWorker() { this(1); }
		
		public SubQueryWorker(int batchSize) {
			this.batchSize = batchSize;
		}
		
		@Override
		public void run() {
			
			WindowAPI wAPI;// = new LocalApi(this);
			
			while (true) {
			
				Queue<DataTuple> batch = new LinkedList<>();
				
				try {
					for (int i = 0; i < this.batchSize; i++)
						batch.add(inputQueues.get(inQueueIndex).take());
	
				} catch (InterruptedException e) { 
					//TODO: put batch back to input queue
				}
				
				/*
				 * drainTo does not block
				 */
				//inputQueues.get(queueIndex).drainTo(batch, batchSize);
				
				inQueueIndex = (inQueueIndex++) % inputQueues.values().size();
					
//				for (DataTuple tuple : batch)
//					op.processData(tuple, wAPI);
				
			}
		}

		public void send(DataTuple dt) {
			
			if (parent.isMostLocalDownstream()) {
//				parent.getParentMultiOperator().send(dt);
			}
			else {
				try {
					outputQueues.get(outQueueIndex).put(dt);
				} catch (InterruptedException e) { 
					//TODO: notify microOp and multiOp about failure
				}
				outQueueIndex = (outQueueIndex++) % outputQueues.values().size();
			}
		}
	}
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id) {
		this.id = id;
		this.inputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
		this.outputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
	}
	
	
	public void execute(ExecutorService executorService, int numberThreads, int batchSize) {
		for (int i = 0; i < numberThreads; i++)
			executorService.execute(new SubQueryWorker(batchSize));
	}

	public int getId() {
		return id;
	}

	public void registerInputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.inputQueues.put(streamId, queue);
	}

	public void registerOutputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.outputQueues.put(streamId, queue);
	}

	public void pushData(DataTuple tuple) {
		/*
		 * This method is only used to push data
		 * to the most upstream operator, which cannot
		 * have more than one input queue		
		 */
		try {
			this.inputQueues.values().iterator().next().put(tuple);
		} catch (InterruptedException e) {
			//TODO: notify microOp and multiOp about failure
		}
	}
	public static SubQuery newSubQuery(
			Set<IMicroOperatorConnectable> microOperators, int opId) {
		return null;
	}


	public void setParentSubQueryConnectable(
			ISubQueryConnectable subQueryConnectable) {
		this.parent = subQueryConnectable;
	};

	
}
