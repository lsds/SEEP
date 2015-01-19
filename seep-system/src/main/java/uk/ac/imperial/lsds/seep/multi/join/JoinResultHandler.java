package uk.ac.imperial.lsds.seep.multi.join;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.Utils;

public class JoinResultHandler {

	public final int SLOTS = Utils.TASKS;

	public AtomicReferenceArray<IQueryBuffer> results = new AtomicReferenceArray<>(
			SLOTS);

	public AtomicIntegerArray slots;
	public AtomicIntegerArray firstOffsets;
	public AtomicIntegerArray secondOffsets;
	public int next;
	
	public IQueryBuffer firstFreeBuffer;
	public IQueryBuffer secondFreeBuffer;

	public JoinResultHandler(IQueryBuffer firstFreeBuffer, IQueryBuffer secondFreeBuffer) {
		this.firstFreeBuffer = firstFreeBuffer;
		this.secondFreeBuffer = secondFreeBuffer;
		slots = new AtomicIntegerArray(SLOTS);
		firstOffsets = new AtomicIntegerArray(SLOTS);
		secondOffsets = new AtomicIntegerArray(SLOTS);
		next = 0;
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, 1);
			firstOffsets.set(i, -1);
			secondOffsets.set(i, -1);
		}
	}
}
