package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;

public class SubQueryTaskSubmitter implements Runnable, IRunningSubQueryTaskHandler {
	
	private SubQueryTaskCreationScheme creationScheme;

	private ISubQueryConnectable subQuery;

	private List<SubQueryTask> runningSubQueryTasks;

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
			SubQueryTask task = creationScheme.next();
			/*
			 * Submit the tasks
			 */
			this.subQuery.getParentMultiOperator().getExecutorService().execute(task);
			runningSubQueryTasks.add(task);
		}
	}

	@Override
	public List<SubQueryTask> getRunningSubQueryTasks() {
		return this.runningSubQueryTasks;
	}


}
