package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTaskResult;

public interface IRunningSubQueryTaskHandler {
	
	public List<Future<SubQueryTaskResult>> getRunningSubQueryTasks();

}
