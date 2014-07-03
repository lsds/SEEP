package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Iterator;

public interface SubQueryTaskCreationScheme extends Iterator<SubQueryTask> {
	
	public void init(ISubQueryConnectable subQueryConnectable, int lastProcessed);
	
}
