package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.LinkedList;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowAPI;

public class SubQueryWorker  {
	
//	private int inQueueIndex = 0;
//	private int outQueueIndex = 0;
//	private int batchSize;
//	
//	public SubQueryWorker() { this(1); }
//	
//	public SubQueryWorker(int batchSize) {
//		this.batchSize = batchSize;
//	}
//	
//	@Override
//	public void run() {
//		
//		WindowAPI wAPI;// = new LocalApi(this);
//		
//		while (true) {
//		
//			Queue<DataTuple> batch = new LinkedList<>();
//			
//			try {
//				for (int i = 0; i < this.batchSize; i++)
//					batch.add(inputQueues.get(inQueueIndex).take());
//
//			} catch (InterruptedException e) { 
//				//TODO: put batch back to input queue
//			}
//			
//			/*
//			 * drainTo does not block
//			 */
//			//inputQueues.get(queueIndex).drainTo(batch, batchSize);
//			
//			inQueueIndex = (inQueueIndex++) % inputQueues.values().size();
//				
////			for (DataTuple tuple : batch)
////				op.processData(tuple, wAPI);
//			
//		}
//	}
//
//	public void send(DataTuple dt) {
//		
//		if (parent.isMostLocalDownstream()) {
////			parent.getParentMultiOperator().send(dt);
//		}
//		else {
//			try {
//				outputQueues.get(outQueueIndex).put(dt);
//			} catch (InterruptedException e) { 
//				//TODO: notify microOp and multiOp about failure
//			}
//			outQueueIndex = (outQueueIndex++) % outputQueues.values().size();
//		}
//	}
}