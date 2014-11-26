package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ResultHandler {
	
	public final int SLOTS = Utils.TASKS;
	
	public AtomicIntegerArray slots;
	public AtomicIntegerArray offsets;
	public int next;
	
	public ResultHandler () {
		slots = new AtomicIntegerArray (SLOTS);
		offsets = new AtomicIntegerArray (SLOTS);
		for (int i = 0; i < SLOTS; i++) {
			slots.set(i, 1);
			offsets.set(i, -1);
		}
	}
}
