package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition;

public class WindowBatchTaskCreationScheme implements
		SubQueryTaskCreationScheme {

	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));

	private int lastProcessed;
	
	private SubQueryBuffer input;
	
	private Iterator<SubQueryTask> iter;
	
	public WindowBatchTaskCreationScheme(){
		
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public SubQueryTask next() {
		return iter.next();
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException("");
	}
	@Override
	public void init(SubQueryBuffer buffer, int lastProcessed, ISubQueryConnectable subQueryConnectable) {
////		WindowDefinition winDef = subQueryConnectable.getSubQuery().getWindowDefinitions()
////		
////		this.subQueryConnectable = subQueryConnectable;
//		this.lastProcessed = lastProcessed;
		
	}

}
