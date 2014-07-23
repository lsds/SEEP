package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public class SubQueryTaskResultFetcher implements Runnable {
	
	private long lastFinishedOrderID = -1;

	private IRunningSubQueryTaskHandler runningSubQueryTaskHandler;

	private ISubQueryTaskResultForwarder resultForwarder;

	public SubQueryTaskResultFetcher(IRunningSubQueryTaskHandler runningSubQueryTaskHandler, ISubQueryTaskResultForwarder resultForwarder) {
		this.runningSubQueryTaskHandler = runningSubQueryTaskHandler;
		this.resultForwarder = resultForwarder;
	}

	@Override
	public void run() {
		
		/*
		 * Process all tasks that are finished and have consecutive logical order ids,
		 * starting from the last known one
		 */
		List<Integer> lIds = new ArrayList<>(runningSubQueryTaskHandler.getCompletedSubQueryTasks().keySet());
		Collections.sort(lIds);
		for (int lId : lIds) {
			Future<SubQueryTaskResult> future = runningSubQueryTaskHandler.getCompletedSubQueryTasks().get(lId);
			if (this.lastFinishedOrderID == lId - 1) {
				// Remove from running tasks
				runningSubQueryTaskHandler.getCompletedSubQueryTasks().remove(lId);
				// Get result
				try {
					SubQueryTaskResult result = future.get();
					List<DataTuple> resultStream = result.getResultStream();
					/*
					 * Forward the result using the appropriate mechanism,
					 * either writing to the downstream sub query buffer or
					 * by sending to distributed nodes via the API of the 
					 * MultiOperator
					 */
					this.resultForwarder.forwardResult(resultStream);
					
					// Record progress by updating the last finished pointer
					this.lastFinishedOrderID = result.getLogicalOrderID();
					// free data in the buffer over which the task was defined
					result.freeIndicesInBuffers();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
				/*
				 * We are missing the result of the task with the next logical order id,
				 * so we cannot further process the finished tasks
				 */
				break;
		}
	}
}
