package uk.ac.imperial.lsds.seep.multi;

public class PartialWindowResults {
	
	int pid;
	
	IQueryBuffer buffer;
	
	boolean complete;
	
	boolean empty;
	
	int size;
	
	public PartialWindowResults (int pid) {
		
		this.pid = pid;
		
		this.buffer = null;
		this.complete = false;
		this.size = 0;
	}
	
	public IQueryBuffer getBuffer() {
		return buffer;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public boolean isEmpty () {
		return (this.size == 0);
	}
	
	public void release() {
		PartialWindowResultsFactory.free(this.pid, this);
	}
	
	public void init() {
	}
}
