package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Iterator;

import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public interface SubQueryTaskCreationScheme extends Iterator<SubQueryTask> {
	
	public void init(SubQueryBuffer buffer, int lastProcessed, ISubQueryConnectable subQueryConnectable);
	
}
