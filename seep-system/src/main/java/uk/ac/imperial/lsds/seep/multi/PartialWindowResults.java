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
		return (this.buffer.position() == 0);
	}
	
	public void release() {
		if (buffer != null) {
			buffer.release();
		} else {
			
			System.err.println("error: buffer is null");
			System.exit(1);
		}
		buffer = null;
		PartialWindowResultsFactory.free(this.pid, this);
	}
	
	public void init() {
		if (buffer != null) {
			System.err.println("error: buffer is not null");
			System.exit(1);
		}
		this.buffer = null;
		this.complete = false;
		this.size = 0;
	}

	public void setBuffer(IQueryBuffer buffer) {
		this.buffer = buffer;
	}
}
