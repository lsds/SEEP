package uk.ac.imperial.lsds.seep.operator.compose.OLD;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskSubmitterOLD implements Runnable, IRunningSubQueryTaskHandler {
	
	private SubQueryTaskCreationScheme creationScheme;

	private ISubQueryConnectable subQuery;

	private List<Future<SubQueryTaskResultOLD>> runningSubQueryTasks;
	
	private Map<Integer, Future<SubQueryTaskResultOLD>> completedSubQueryTasks;
	
	long finished  = 0L;
	long target    = 0L;
	
	public SubQueryTaskSubmitterOLD(ISubQueryConnectable subQuery, SubQueryTaskCreationScheme creationScheme) {
		this.subQuery = subQuery;
		this.creationScheme = creationScheme;
		this.runningSubQueryTasks = new LinkedList<>();
		this.completedSubQueryTasks = new HashMap<>();
		
		this.target = this.subQuery.getParentMultiOperator().getTarget();
		System.out.println(String.format("Target is %d tasks", target));
	}

//	private Monitor monitor = new Monitor();
	
	public void run() {
		
//		monitor.monitor("Submitted and not completed: " + runningSubQueryTasks.size() + "\t Completed and not forwarded: " + completedSubQueryTasks.keySet().size());

		/*
		 * For each running task, check whether it has terminated and collect
		 * those that have finished
		 */
		Iterator<Future<SubQueryTaskResultOLD>> runningIter = runningSubQueryTasks.iterator();
		while (runningIter.hasNext()) {
			Future<SubQueryTaskResultOLD> future = runningIter.next();
			if (future.isDone()) {
				try {
					completedSubQueryTasks.put(future.get().getLogicalOrderID(), future);
					runningIter.remove();
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finished++;
				if (finished == target) {
					System.out.println("Done.");
					this.subQuery.getParentMultiOperator().targetReached();
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
			Future<SubQueryTaskResultOLD> future = this.subQuery.getParentMultiOperator().getExecutorService().submit(task);
			runningSubQueryTasks.add(future);
		}
	}

	@Override
	public Map<Integer, Future<SubQueryTaskResultOLD>> getCompletedSubQueryTasks() {
		return this.completedSubQueryTasks;
	}
}
