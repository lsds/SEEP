package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.WindowBatchTaskCreationScheme;

public class SubQueryBufferHandler implements Runnable {

	public static final int SUB_QUERY_QUEUE_CAPACITY = Integer.valueOf(GLOBALS.valueFor("subQueryQueueCapacity"));


	private SubQueryBuffer buffer;
	
	private int lastStartedPointer;
	private int lastFinishedOrderID;
	private SubQueryTaskCreationScheme creationScheme;
	private List<SubQueryTask> runningSubQueryTasks = new LinkedList<SubQueryTask>();

	private ISubQueryConnectable upstreamSubQuery;
	private ISubQueryConnectable downstreamSubQuery;
	
	public SubQueryBufferHandler(ISubQueryConnectable upstreamSubQuery, ISubQueryConnectable downstreamSubQuery) {
		this.upstreamSubQuery = upstreamSubQuery;
		this.downstreamSubQuery = downstreamSubQuery;
		this.buffer = new SubQueryBuffer(SUB_QUERY_QUEUE_CAPACITY);
		//TODO: select creation scheme based on query semantics
		this.creationScheme = new WindowBatchTaskCreationScheme();
	}

	
	@Override
	public void run() {
		
		/*
		 * Busy waiting
		 */
		while (true) {
			
			/*
			 * For each of the buffer handlers of incoming streams for the sub query that
			 * writes to this buffer
			 */
			for (SubQueryBufferHandler upHandler : upstreamSubQuery.getLocalUpstreamBufferHandlers()) {
				/*
				 * For each running task, check whether it has terminated
				 */
				Iterator<SubQueryTask> taskIter = upHandler.getRunningSubQueryTasks().iterator();
				
				/*
				 * Collect all that have finished
				 */
				Map<Integer, SubQueryTask> finished = new HashMap<>();
				while (taskIter.hasNext()) {
					SubQueryTask task = taskIter.next();
					if (task.isDone())
						finished.put(task.getLogicalOrderID(), task);
				}
				
				/*
				 * Process all tasks that are finished and have consecutive logical order ids,
				 * starting from the last known one
				 */
				List<Integer> lIds = new ArrayList<>(finished.keySet());
				Collections.sort(lIds);
				for (int lId : lIds) {
					SubQueryTask task = finished.get(lId);
					if (upHandler.getLastFinishedOrderID() == lId - 1) {
						// Remove from running tasks
						upHandler.getRunningSubQueryTasks().remove(task);
						// Get result
						try {
							List<DataTuple> result = task.get();
							// Update the respective queues with the result
							for (Entry<Integer, ISubQueryConnectable> downstreamEntry : task.getSubQueryConnectable().getLocalDownstream().entrySet()) {
								downstreamEntry.getValue().getSubQuery().pushData(result, downstreamEntry.getKey());
							}
							// Record progress by updating the last finished pointer
							upHandler.setLastFinishedOrderID(task.getLogicalOrderID());
						} catch (Exception e) {
							// TODO: handle exception
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
			
			/*
			 * Check whether a task should be instantiated
			 */
			this.creationScheme.init(this.downstreamSubQuery, lastStartedPointer);
			while (creationScheme.hasNext()) {
				SubQueryTask task = creationScheme.next();
				/*
				 * Submit the tasks
				 */
				this.upstreamSubQuery.getParentMultiOperator().getExecutorService().execute(task);
				runningSubQueryTasks.add(task);
				lastStartedPointer = task.getLastProcessed();
			}

			
		}
		
	}

	public List<SubQueryTask> getRunningSubQueryTasks() {
		return runningSubQueryTasks;
	}

	public int getLastFinishedOrderID() {
		return lastFinishedOrderID;
	}

	public void setLastFinishedOrderID(int lastFinishedOrderID) {
		this.lastFinishedOrderID = lastFinishedOrderID;
	}

	public SubQueryBuffer getBuffer() {
		return buffer;
	}

}
