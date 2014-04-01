package uk.ac.imperial.lsds.java2sdg.bricks.SDG;

public final class Stream {

	private final int id;
	private final int workflowId;
	private final StreamType type;
	
	public Stream(int id, int workflowId, StreamType type){
		this.id = id;
		this.workflowId = workflowId;
		this.type = type;
	}
	
	public int getId(){
		return id;
	}
	
	public int getWorkflowId(){
		return workflowId;
	}
	
	public StreamType getType(){
		return type;
	}
}
