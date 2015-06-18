package uk.ac.imperial.lsds.seep.multi;

public class PartialWindowResults {
	
	int pid; /* The worker that requested this object. */
	
	IQueryBuffer buffer; /* The buffer that holds the partial window results */
	
	boolean empty;
	
	int size;
	
	public PartialWindowResults (int pid) {
		
		this.pid = pid;
		
		this.buffer = null;
		this.size = 0;
	}
	
	public void setBuffer(IQueryBuffer buffer) {
		this.buffer = buffer;
		this.size = buffer.position();
	}
	
	public IQueryBuffer getBuffer () {
		return buffer;
	}
	
	public boolean isEmpty () {
		if (buffer == null)
			return true;
		else
			return (this.buffer.position() == 0);
	}
	
	public void release () {
		
		if (buffer != null) {
			buffer.release();
		}
		buffer = null;
		size = 0;
		PartialWindowResultsFactory.free(this.pid, this);
	}
	
	public void init() {
		
		this.buffer = null;
		this.size = 0;
	}
}
