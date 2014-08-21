package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryTaskResultForwarder;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.ResultHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

public abstract class AbstractSubQueryTask implements ISubQueryTask {

	/*
	 * Data for handling the task after it completed
	 */
	private Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices;
	private int logicalOrderID;

	/*
	 * Input data for the actual execution of the task
	 */
	protected ISubQueryConnectable subQueryConnectable;
	protected Map<Integer, IWindowBatch> windowBatches;

	/*
	 * Output data produced by the task
	 */
	protected MultiOpTuple[] resultStream;
	
	protected AbstractSubQueryTask(
			ISubQueryConnectable subQueryConnectable, 
			Map<Integer, IWindowBatch> windowBatches, 
			int logicalOrderID, 
			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices) {
		this.subQueryConnectable = subQueryConnectable;
		this.windowBatches = windowBatches;
		this.logicalOrderID = logicalOrderID;
		this.freeUpToIndices = freeUpToIndices;
	}
	
	protected void pushResults() {
		
		/*
		 * Computation has finished, so we can immediately free the data
		 * in the input buffers
		 */
		for (SubQueryBufferWindowWrapper b : this.freeUpToIndices.keySet())
			b.freeUpToIndexInBuffer(this.freeUpToIndices.get(b));
		
		/*
		 * Push data to all the forwarders registered for the sub query
		 */
		for (ISubQueryTaskResultForwarder forwarder : this.subQueryConnectable.getResultForwarders()) {
			ResultHandler handler = forwarder.getResultHandler();
			
			try {
				int insertIndex = logicalOrderID % ResultHandler.NUMBER_RESULT_SLOTS;
				
				boolean gotNextIndexToPush = (insertIndex == handler.nextToPush);
				
				/*
				 * Record the result. We expect the result buffer to be empty at the respective position. 
				 * If that is not the case, we have to wait (should not happen though).
				 */
				while (!handler.results.compareAndSet(insertIndex, null, this.resultStream))
					Thread.sleep(1);
				
				/*
				 * Try to get the lock for pushing data to this source
				 */
				if (handler.pushOngoing.compareAndSet(false, true)) {
					MultiOpTuple[] result = handler.results.getAndSet(handler.nextToPush, null);
					
					while (result != null) {
						handler.nextToPush = (handler.nextToPush + 1) % ResultHandler.NUMBER_RESULT_SLOTS;					
						forwarder.forwardResult(result);
						result = handler.results.getAndSet(handler.nextToPush, null);
					}
					
					/*
					 * Release push lock
					 */
					handler.pushOngoing.set(false);
				}
				else if (gotNextIndexToPush) {
					/*
					 * A concurrent thread was pushing while we inserted the next to push. Hence,
					 * we cannot be know whether or not the concurrent thread pushed our result. So,
					 * we need to check whether it was pushed. 
					 */
					//handler.nextToPushLock
					
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	private void actualPush (ISubQueryTaskResultForwarder forwarder, ResultHandler handler) {
		MultiOpTuple[] result = handler.results.getAndSet(handler.nextToPush, null);
		
		while (result != null) {
			handler.nextToPush = (handler.nextToPush + 1) % ResultHandler.NUMBER_RESULT_SLOTS;					
			forwarder.forwardResult(result);
			result = handler.results.getAndSet(handler.nextToPush, null);
		}
		
		/*
		 * Release push lock
		 */
		handler.pushOngoing.set(false);

	}
	
}
