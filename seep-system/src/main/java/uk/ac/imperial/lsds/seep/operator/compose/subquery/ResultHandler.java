package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

public class ResultHandler {

	public static final int NUMBER_RESULT_SLOTS = 50;

	public AtomicReferenceArray<MultiOpTuple[]> results = new AtomicReferenceArray<MultiOpTuple[]>(NUMBER_RESULT_SLOTS); 
	public AtomicIntegerArray freeResultSlots = new AtomicIntegerArray(NUMBER_RESULT_SLOTS); 
	
	public int nextToPush = 0;
	
	public ResultHandler() {
		for (int i = 0; i < freeResultSlots.length(); i++)
			freeResultSlots.set(i, 1);
	}
	
}
