package uk.ac.imperial.lsds.seep.multi;

public class PartialWindowResults {
	
	int pid;
	
	IQueryBuffer buffer;
	
	boolean complete;
	
	boolean empty;
	
	public PartialWindowResults (int pid) {
		
		this.pid = pid;
		
		this.buffer = null;
		this.complete = false;
		this.empty = true;
	}
	
	public IQueryBuffer getBuffer() {
		return buffer;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public boolean isEmpty () {
		return empty;
	}
	
	public void release() {
		PartialWindowResultsFactory.free(this.pid, this);
	}
	
	public void init() {
	}
}
