package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.LocalApi;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class StatelessMicroOperator implements MicroOperator {

	Map<Integer, BlockingQueue<DataTuple>> inputQueues;
	
	Map<Integer, BlockingQueue<DataTuple>> outputQueues;
	
	OperatorCode op;
	
	private int id;
	
	LocalConnectable parent;
	
	public class OPWorker implements Runnable, API, Callback {
		
		private int inQueueIndex = 0;
		private int outQueueIndex = 0;
		private int batchSize;
		
		public OPWorker() { this(1); }
		
		public OPWorker(int batchSize) {
			this.batchSize = batchSize;
		}
		
		@Override
		public void run() {
			
			API localApi = new LocalApi(this);
			
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
					
				for (DataTuple tuple : batch)
					op.processData(tuple, localApi);
				
			}
		}

		@Override
		public void setCallbackObject(Callback c) { }

		@Override
		public void send(DataTuple dt) {
			
			if (parent.isMostLocalDownstream())
				parent.getParentMultiOperator().send(dt);
			else {
				try {
					outputQueues.get(outQueueIndex).put(dt);
				} catch (InterruptedException e) { 
					//TODO: notify microOp and multiOp about failure
				}
				outQueueIndex = (outQueueIndex++) % outputQueues.values().size();
			}
		}

		@Override
		public void send_toIndex(DataTuple dt, int idx) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_splitKey(DataTuple dt, int key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_toStreamId_toAll(DataTuple dt, int streamId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_all(DataTuple dt) {
			if (parent.isMostLocalDownstream())
				parent.getParentMultiOperator().send(dt);
			else {
				try {
					for (BlockingQueue<DataTuple> q : outputQueues.values()) 
						q.put(dt);
				} catch (InterruptedException e) { 
					//TODO: notify microOp and multiOp about failure
				}
			}
		}

		@Override
		public void send_toStreamId(DataTuple dt, int streamId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_all_threadPool(DataTuple dt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_to_OpId(DataTuple dt, int opId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_to_OpIds(DataTuple[] dt, int[] opId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void send_toIndices(DataTuple[] dts, int[] indices) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private StatelessMicroOperator(OperatorCode op, int id) {
		this.op = op;
		this.id = id;
		this.inputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
		this.outputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
	}
	
	@Override
	public void setParentLocalConnectable(LocalConnectable parent) {
		this.parent = parent;
	}
	
	public static MicroOperator newStatelessMicroOperator(OperatorCode op, int id) {
		return new StatelessMicroOperator(op, id);
	}
	
	
	@Override
	public void execute(ExecutorService executorService, int numberThreads, int batchSize) {
		for (int i = 0; i < numberThreads; i++)
			executorService.execute(new OPWorker(batchSize));
	}

	@Override
	public int getMicroOperatorId() {
		return id;
	}

	@Override
	public void registerInputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.inputQueues.put(streamId, queue);
	}

	@Override
	public void registerOutputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.outputQueues.put(streamId, queue);
	}

	@Override
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
	
}
