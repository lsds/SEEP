package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;

public class DownstreamSubQueryBufferHandler implements ISubQueryBufferHandler {

	private SubQueryBuffer buffer;

	private int lastFinishedOrderID = 0;
	
	private ISubQueryConnectable upstreamSubQuery;
	
	public DownstreamSubQueryBufferHandler(ISubQueryConnectable upstreamSubQuery) {
		this.upstreamSubQuery = upstreamSubQuery;
		this.buffer = new SubQueryBuffer(SubQueryBuffer.SUB_QUERY_BUFFER_CAPACITY);
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
			for (IUpstreamSubQueryBufferHandler upHandler : upstreamSubQuery.getLocalUpstreamBufferHandlers()) {
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
					if (this.lastFinishedOrderID == lId - 1) {
						// Remove from running tasks
						upHandler.getRunningSubQueryTasks().remove(task);
						try {
							/*
							 *  Send the result using the API of the parent MultiOperator
							 */
							for (DataTuple tuple : task.get()) {
								this.upstreamSubQuery.getParentMultiOperator().getAPI().send(tuple);
							}
							
							// Record progress by updating the last finished pointer
							this.lastFinishedOrderID = task.getLogicalOrderID();
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
			
		}
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.lsds.seep.operator.compose.multi.IBufferHandler#getBuffer()
	 */
	@Override
	public SubQueryBuffer getBuffer() {
		return buffer;
	}


}
