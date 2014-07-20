package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public class SubQueryTaskSubmitter implements Runnable, IRunningSubQueryTaskHandler {
	
	private SubQueryTaskCreationScheme creationScheme;

	private ISubQueryConnectable subQuery;

	private List<Future<SubQueryTaskResult>> runningSubQueryTasks;

	public SubQueryTaskSubmitter(ISubQueryConnectable subQuery, SubQueryTaskCreationScheme creationScheme) {
		this.subQuery = subQuery;
		this.creationScheme = creationScheme;
		this.runningSubQueryTasks = new LinkedList<>();
	}
	
	public void run() {
		/*
		 * Check whether a task should be instantiated
		 */
		this.creationScheme.createTasks();
		while (creationScheme.hasNext()) {
			SubQueryTaskCallable task = creationScheme.next();
			/*
			 * Submit the tasks
			 */
			Future<SubQueryTaskResult> future = this.subQuery.getParentMultiOperator().getExecutorService().submit(task);
			runningSubQueryTasks.add(future);
		}
	}

	@Override
	public List<Future<SubQueryTaskResult>> getRunningSubQueryTasks() {
		return this.runningSubQueryTasks;
	}


}
