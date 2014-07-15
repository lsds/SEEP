package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.WindowBatchTaskCreationScheme;

public class UpstreamSubQueryBufferHandler implements IUpstreamSubQueryBufferHandler {

	private SubQueryBuffer buffer;
	
	private int lastStartedPointer = 0;
	private SubQueryTaskCreationScheme creationScheme;
	private List<SubQueryTask> runningSubQueryTasks = new LinkedList<SubQueryTask>();

	private ISubQueryConnectable downstreamSubQuery;
	
	public UpstreamSubQueryBufferHandler(ISubQueryConnectable downstreamSubQuery) {
		this.downstreamSubQuery = downstreamSubQuery;
		this.buffer = new SubQueryBuffer(SubQueryBuffer.SUB_QUERY_BUFFER_CAPACITY);
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
			 * Check whether a task should be instantiated
			 */
			this.creationScheme.init(this.buffer, lastStartedPointer, this.downstreamSubQuery);
			while (creationScheme.hasNext()) {
				SubQueryTask task = creationScheme.next();
				/*
				 * Submit the tasks
				 */
				this.downstreamSubQuery.getParentMultiOperator().getExecutorService().execute(task);
				runningSubQueryTasks.add(task);
				lastStartedPointer = task.getLastProcessed();
			}
		}
	}


	@Override
	public SubQueryBuffer getBuffer() {
		return this.buffer;
	}


	@Override
	public List<SubQueryTask> getRunningSubQueryTasks() {
		return this.runningSubQueryTasks;
	}

}
