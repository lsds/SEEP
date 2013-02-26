package seep.operator;

import java.io.Serializable;

public abstract class State implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
	public State clone(){
		
		try {
			return (State) super.clone();
		}
		catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private int ownerId;
	private String stateTag;
	private State stateImpl;
	private long data_ts;
	private int checkpointInterval;
		
	public void setStateTag(String stateTag){
		this.stateTag = stateTag;
	}
	
	public int getCheckpointInterval(){
		return checkpointInterval;
	}
	
	public void setCheckpointInterval(int checkpointInterval){
		this.checkpointInterval = checkpointInterval;
	}
	
	public String getStateTag(){
		return stateTag;
	}
	
	public int getOwnerId(){
		return ownerId;
	}
	
	public void setOwnerId(int ownerId){
		this.ownerId = ownerId;
	}
	
	public long getData_ts(){
		return data_ts;
	}
	
	public void setData_ts(long data_ts){
		this.data_ts = data_ts;
	}
	
	public State getStateImpl(){
		return stateImpl;
	}
	
	public State(){
		// Empty constructor for serialization purposes
	}
	
	public State(State toCopy){
		//This copy-constructor wont be used for anything more than copying
		this.checkpointInterval = 0;
		this.ownerId = toCopy.ownerId;
		this.stateTag = toCopy.stateTag;
		this.stateImpl = toCopy.stateImpl;
		this.data_ts = toCopy.data_ts;
	}
	
	//TODO by now checkpoints will be performed only temporarily
	public State(int ownerId, int checkpointInterval){
		// Mandatory variables to initialize a state
		this.ownerId = ownerId;
		this.checkpointInterval = checkpointInterval;
	}
	
	public State(int ownerId, int checkpointInterval, State stateImpl){
		this.ownerId = ownerId;
		this.checkpointInterval = checkpointInterval;
		this.stateImpl = stateImpl;
	}
	
	public abstract State[] splitState(State toSplit, int key);
}
