package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.WindowBatchTaskCreationScheme;

public class SubQueryBufferHandler {
//
//	private SubQueryBuffer buffer;
//	
//	private Map<Integer, Long> nextToProcessPointers;
//	private long lastFinishedOrderID = -1;
//	private SubQueryTaskCreationScheme creationScheme;
//	private List<SubQueryTask> runningSubQueryTasks = new LinkedList<SubQueryTask>();
//
//	private ISubQueryConnectable upstreamSubQuery;
//	private ISubQueryConnectable downstreamSubQuery;
//	
//	public SubQueryBufferHandler(ISubQueryConnectable upstreamSubQuery, ISubQueryConnectable downstreamSubQuery) {
//		this.upstreamSubQuery = upstreamSubQuery;
//		this.downstreamSubQuery = downstreamSubQuery;
//		this.buffer = new SubQueryBuffer(SubQueryBuffer.SUB_QUERY_BUFFER_CAPACITY);
//		//TODO: select creation scheme based on query semantics
//		this.creationScheme = new WindowBatchTaskCreationScheme();
//		this.nextToProcessPointers = new HashMap<>();
//		for (Integer streamID : this.downstreamSubQuery.getLocalUpstream().keySet())
//			this.nextToProcessPointers.put(streamID, -1l);
//		
//	}
//
//	
//	@Override
//	public void run() {
//		
//		/*
//		 * Busy waiting
//		 */
//		while (true) {
//			
//			/*
//			 * For each of the buffer handlers of incoming streams for the sub query that
//			 * writes to this buffer
//			 */
//			for (IUpstreamSubQueryBufferHandler upHandler : upstreamSubQuery.getLocalUpstreamBufferHandlers()) {
//				/*
//				 * For each running task, check whether it has terminated
//				 */
//				Iterator<SubQueryTask> taskIter = upHandler.getRunningSubQueryTasks().iterator();
//				
//				/*
//				 * Collect all that have finished
//				 */
//				Map<Long, SubQueryTask> finished = new HashMap<>();
//				while (taskIter.hasNext()) {
//					SubQueryTask task = taskIter.next();
//					if (task.isDone())
//						finished.put(task.getLogicalOrderID(), task);
//				}
//				
//				/*
//				 * Process all tasks that are finished and have consecutive logical order ids,
//				 * starting from the last known one
//				 */
//				List<Long> lIds = new ArrayList<>(finished.keySet());
//				Collections.sort(lIds);
//				for (long lId : lIds) {
//					SubQueryTask task = finished.get(lId);
//					if (this.lastFinishedOrderID == lId - 1) {
//						// Remove from running tasks
//						upHandler.getRunningSubQueryTasks().remove(task);
//						// Get result
//						try {
//							List<DataTuple> result = task.get();
//							// Update the buffer with the result
//							for (ISubQueryBufferHandler handler : task.getSubQueryConnectable().getLocalDownstreamBufferHandlers()) {
//								List<DataTuple> notAdded = handler.getBuffer().add(result);
//								while (!notAdded.isEmpty()) {
//									try {
//										handler.getBuffer().wait();
//										notAdded = handler.getBuffer().add(notAdded);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//							// Record progress by updating the last finished pointer
//							this.lastFinishedOrderID = task.getLogicalOrderID();
//							// free data in the buffer over which the task was defined
//							upHandler.getBuffer().f
//							
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}
//					else
//						/*
//						 * We are missing the result of the task with the next logical order id,
//						 * so we cannot further process the finished tasks
//						 */
//						break;
//				}
//			}
//			
//			/*
//			 * Check whether a task should be instantiated
//			 */
//			this.nextToProcessPointers = this.creationScheme.createTasks(this.buffer, this.nextToProcessPointers, this.downstreamSubQuery);
//			while (creationScheme.hasNext()) {
//				SubQueryTask task = creationScheme.next();
//				/*
//				 * Submit the tasks
//				 */
//				this.downstreamSubQuery.getParentMultiOperator().getExecutorService().execute(task);
//				runningSubQueryTasks.add(task);
//			}
//
//			
//		}
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see uk.ac.imperial.lsds.seep.operator.compose.multi.IBufferHandler#getRunningSubQueryTasks()
//	 */
//	@Override
//	public List<SubQueryTask> getRunningSubQueryTasks() {
//		return runningSubQueryTasks;
//	}
//
//	/* (non-Javadoc)
//	 * @see uk.ac.imperial.lsds.seep.operator.compose.multi.IBufferHandler#getBuffer()
//	 */
//	@Override
//	public SubQueryBuffer getBuffer() {
//		return buffer;
//	}

}
