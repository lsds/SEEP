package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryTaskResultForwarder;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;

public class ResultCollector {

	private Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices;
	private int logicalOrderID;
	private ISubQueryConnectable subQueryConnectable;

	public ResultCollector(
			ISubQueryConnectable subQueryConnectable,
			int logicalOrderID,
			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices) {

		this.subQueryConnectable = subQueryConnectable;
		this.logicalOrderID = logicalOrderID;
		this.freeUpToIndices = freeUpToIndices;
	}

	
	protected void pushResults(MultiOpTuple[] resultStream) {
		
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
				
				/*
				 * Record the result. We expect the result buffer to be empty at the respective position. 
				 * If that is not the case, we have to wait (should not happen though).
				 */
				while (!handler.freeResultSlots.compareAndSet(insertIndex, 1, 0))
					Thread.sleep(1);

				handler.results.set(insertIndex, resultStream);
				
				MultiOpTuple[] result = handler.results.getAndSet(handler.nextToPush, null);
				
				while (result != null) {
					forwarder.forwardResult(result);
					int forwardedIndex = handler.nextToPush;
					handler.nextToPush = (handler.nextToPush + 1) % ResultHandler.NUMBER_RESULT_SLOTS;
					handler.freeResultSlots.set(forwardedIndex, 1);
					result = handler.results.getAndSet(handler.nextToPush, null);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	
	
}
