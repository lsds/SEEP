package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryTaskCallable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public class SubQueryTaskSubmitter implements IRunningSubQueryTaskHandler {
	
	private SubQuery subQuery;

	private List<Future<SubQueryTaskResult>> runningSubQueryTasks;
	
	private Map<Integer, Future<SubQueryTaskResult>> completedSubQueryTasks;
	
	long finished  = 0L;
	long target    = 0L;
	
	public SubQueryTaskSubmitter(SubQuery subQuery) {
		this.subQuery = subQuery;
		this.runningSubQueryTasks = new LinkedList<>();
		this.completedSubQueryTasks = new HashMap<>();
		
		this.target = this.subQuery.getParent().getParentMultiOperator().getTarget();
		System.out.println(String.format("Target is %d tasks", target));
	}

//	private Monitor monitor = new Monitor();
	
	public void dispatch(ISubQueryTaskCallable task) {
		
		/*
		 * Submit the tasks
		 */
		Future<SubQueryTaskResult> future = this.subQuery.getParent().getParentMultiOperator().getExecutorService().submit(task);
		runningSubQueryTasks.add(future);

		
//		monitor.monitor("Submitted and not completed: " + runningSubQueryTasks.size() + "\t Completed and not forwarded: " + completedSubQueryTasks.keySet().size());

		/*
		 * For each running task, check whether it has terminated and collect
		 * those that have finished
		 */
//		Iterator<Future<SubQueryTaskResult>> runningIter = runningSubQueryTasks.iterator();
//		while (runningIter.hasNext()) {
//			Future<SubQueryTaskResult> future = runningIter.next();
//			if (future.isDone()) {
//				try {
//					completedSubQueryTasks.put(future.get().getLogicalOrderID(), future);
//					runningIter.remove();
//				} catch (InterruptedException | ExecutionException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				finished++;
//				if (finished == target) {
//					System.out.println("Done.");
//					this.subQuery.getParentMultiOperator().targetReached();
//				}
//			}
//		}
		
	}

	@Override
	public Map<Integer, Future<SubQueryTaskResult>> getCompletedSubQueryTasks() {
		return this.completedSubQueryTasks;
	}
}
