package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;

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
		 * For each running task, check whether it has terminated and collect
		 * those that have finished
		 */
		Map<Long, SubQueryTask> finished = new HashMap<>();
		for (SubQueryTask task : runningSubQueryTaskHandler.getRunningSubQueryTasks())
			if (task.isDone())
				finished.put(task.getLogicalOrderID(), task);
		
		/*
		 * Process all tasks that are finished and have consecutive logical order ids,
		 * starting from the last known one
		 */
		List<Long> lIds = new ArrayList<>(finished.keySet());
		Collections.sort(lIds);
		for (long lId : lIds) {
			SubQueryTask task = finished.get(lId);
			if (this.lastFinishedOrderID == lId - 1) {
				// Remove from running tasks
				runningSubQueryTaskHandler.getRunningSubQueryTasks().remove(task);
				// Get result
				try {
					List<DataTuple> result = task.get();
					/*
					 * Forward the result using the appropriate mechanism,
					 * either writing to the downstream sub query buffer or
					 * by sending to distributed nodes via the API of the 
					 * MultiOperator
					 */
					this.resultForwarder.forwardResult(result);
					
					// Record progress by updating the last finished pointer
					this.lastFinishedOrderID = task.getLogicalOrderID();
					// free data in the buffer over which the task was defined
					task.freeIndicesInBuffers();
					
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
