package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicMarkableReference;

public abstract class ITask implements IWindowAPI {
	
	public int taskid;
	public int queryid;
	public AtomicMarkableReference<ITask> next;
	
	/* Previous query executed by the GPU task processor */
	protected SubQuery _query = null;
	
	protected boolean GPU = false;
	
	public abstract int run();
	
	public abstract void free();
	
	public void setGPU (boolean GPU) {
		this.GPU = GPU;
	}
	
	public void setPreviousQuery (SubQuery _query) {
		this._query = _query; 
	}

	public abstract SubQuery getQuery ();
}
