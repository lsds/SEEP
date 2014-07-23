package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.Map;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public interface IRunningSubQueryTaskHandler {
	
	public Map<Integer, Future<SubQueryTaskResult>> getCompletedSubQueryTasks();

}
