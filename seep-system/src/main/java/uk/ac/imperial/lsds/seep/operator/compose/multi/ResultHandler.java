package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ResultHandler {

	public static final int NUMBER_RESULT_SLOTS = 50;

	public AtomicBoolean pushOngoing = new AtomicBoolean(false);

	public AtomicReferenceArray<MultiOpTuple[]> results = new AtomicReferenceArray<MultiOpTuple[]>(NUMBER_RESULT_SLOTS); 
	
	public int nextToPush = 0;
	
	public Object nextToPushLock = new Object();
	
}
