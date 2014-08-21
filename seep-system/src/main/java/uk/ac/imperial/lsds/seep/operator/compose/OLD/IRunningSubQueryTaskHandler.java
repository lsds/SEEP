package uk.ac.imperial.lsds.seep.operator.compose.OLD;

import java.util.Map;
import java.util.concurrent.Future;


public interface IRunningSubQueryTaskHandler {
	
	public Map<Integer, Future<SubQueryTaskResultOLD>> getCompletedSubQueryTasks();

}
