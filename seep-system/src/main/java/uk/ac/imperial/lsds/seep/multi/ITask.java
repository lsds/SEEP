package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicMarkableReference;

public abstract class ITask implements IWindowAPI {
	
	public int taskid;
	public int queryid;
	public AtomicMarkableReference<ITask> next;
	
	protected boolean GPU = false;
	
	public abstract int run();
	
	public abstract void free();
	
	public void setGPU (boolean GPU) {
		this.GPU = GPU;
	}
}
