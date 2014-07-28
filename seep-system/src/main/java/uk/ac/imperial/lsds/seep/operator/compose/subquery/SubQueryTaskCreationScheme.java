package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.List;


public interface SubQueryTaskCreationScheme  {
	
	public List<ISubQueryTaskCallable> createTasks();
	
}
