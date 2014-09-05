package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;

public class ResultHandler {

	public static final int NUMBER_RESULT_SLOTS = 5000;

	public AtomicReferenceArray<MultiOpTuple[]> results = new AtomicReferenceArray<MultiOpTuple[]>(NUMBER_RESULT_SLOTS); 
	public AtomicIntegerArray freeResultSlots = new AtomicIntegerArray(NUMBER_RESULT_SLOTS); 
	
	public List<Map<SubQueryBufferWindowWrapper, Integer>> freeIndicesForResult = new ArrayList<>(NUMBER_RESULT_SLOTS);
	
	public int nextToPush = 0;
	
	public ResultHandler() {
		for (int i = 0; i < NUMBER_RESULT_SLOTS; i++) {
			freeResultSlots.set(i, 1);
			freeIndicesForResult.add(null);
		}
		
		
	}
	
}
