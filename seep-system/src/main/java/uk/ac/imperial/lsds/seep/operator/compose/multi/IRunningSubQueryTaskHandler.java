package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.List;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQueryTask;

public interface IRunningSubQueryTaskHandler {
	
	public List<SubQueryTask> getRunningSubQueryTasks();

}
