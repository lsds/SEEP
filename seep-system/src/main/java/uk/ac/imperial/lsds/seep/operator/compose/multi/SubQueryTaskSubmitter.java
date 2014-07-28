package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryTaskCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskCreationScheme;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public class SubQueryTaskSubmitter implements Runnable, IRunningSubQueryTaskHandler {
	
	private SubQueryTaskCreationScheme creationScheme;

	private ISubQueryConnectable subQuery;

	private List<Future<SubQueryTaskResult>> runningSubQueryTasks;
	
	private Map<Integer, Future<SubQueryTaskResult>> completedSubQueryTasks;

	public SubQueryTaskSubmitter(ISubQueryConnectable subQuery, SubQueryTaskCreationScheme creationScheme) {
		this.subQuery = subQuery;
		this.creationScheme = creationScheme;
		this.runningSubQueryTasks = new LinkedList<>();
		this.completedSubQueryTasks = new HashMap<>();
	}

	private Monitor monitor = new Monitor();
	
	public void run() {
		
		monitor.monitor("Submitted and not completed: " + runningSubQueryTasks.size() + "\t Completed: " + completedSubQueryTasks.keySet().size());

		/*
		 * For each running task, check whether it has terminated and collect
		 * those that have finished
		 */
		Iterator<Future<SubQueryTaskResult>> runningIter = runningSubQueryTasks.iterator();
		while (runningIter.hasNext()) {
			Future<SubQueryTaskResult> future = runningIter.next();
			if (future.isDone()) {
				try {
					completedSubQueryTasks.put(future.get().getLogicalOrderID(), future);
					runningIter.remove();
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
		/*
		 * Check whether a task should be instantiated
		 */
		for (ISubQueryTaskCallable task : this.creationScheme.createTasks()) {
			/*
			 * Submit the tasks
			 */
			Future<SubQueryTaskResult> future = this.subQuery.getParentMultiOperator().getExecutorService().submit(task);
			runningSubQueryTasks.add(future);
		}
	}

	@Override
	public Map<Integer, Future<SubQueryTaskResult>> getCompletedSubQueryTasks() {
		return this.completedSubQueryTasks;
	}
}
