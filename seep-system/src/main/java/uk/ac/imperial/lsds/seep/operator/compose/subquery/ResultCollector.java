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
		
		if (this.freeUpToIndices.values().size() != 1) {
			System.out.println("Result Collector with empty free up to index map");
			if (logicalOrderID != 0)
				System.exit(1);
		}
		
	}

	
	protected void pushResults(MultiOpTuple[] resultStream) {
		
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
				while (!handler.freeResultSlots.compareAndSet(insertIndex, 1, 0)) { 
					System.out.println("I AM WAITING");
					Thread.sleep(1);
				}

				handler.results.set(insertIndex, resultStream);
				handler.freeIndicesForResult.set(insertIndex, this.freeUpToIndices);

				MultiOpTuple[] result = handler.results.getAndSet(handler.nextToPush, null);
				
				while (result != null) {
					//System.out.println("handler.nextToPush " + handler.nextToPush);
					forwarder.forwardResult(result);
					/*
					 * All computation before this window batch has finished, so we can 
					 * free the data in the input buffers
					 */
					Map<SubQueryBufferWindowWrapper, Integer> freeIndicesForForwarded = handler.freeIndicesForResult.get(handler.nextToPush);
					
					if (freeIndicesForForwarded == null) {
						System.out.println("Insert: " + insertIndex + " " + this.freeUpToIndices);
						System.out.println("handler.nextToPush " + handler.nextToPush);
						System.out.println("Keys: " + handler.freeIndicesForResult.toString());
					}
					
					for (SubQueryBufferWindowWrapper b : freeIndicesForForwarded.keySet()) 
						b.freeUpToIndexInBuffer(freeIndicesForForwarded.get(b));

					int forwardedIndex = handler.nextToPush;
					handler.nextToPush = (handler.nextToPush + 1) % ResultHandler.NUMBER_RESULT_SLOTS;
					handler.freeResultSlots.set(forwardedIndex, 1);
					result = handler.results.getAndSet(handler.nextToPush, null);
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}	
	}

	
	
}
