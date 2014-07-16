package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Iterator;
import java.util.Map;

public interface SubQueryTaskCreationScheme extends Iterator<SubQueryTask> {
	
	public Map<Integer, Long> createTasks(ISubQueryConnectable subQueryConnectable, Map<Integer, Long> nextToProcessPointers);
	
}
