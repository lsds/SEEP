package seep.operator;

public abstract class State {

	private final int ownerId;
	private String stateTag;
	private State stateImpl;
	private long data_ts;
	private final int checkpointInterval;
		
	public void setStateTag(String stateTag){
		this.stateTag = stateTag;
	}
	
	public int getCheckpointInterval(){
		return checkpointInterval;
	}
	
	public String getStateTag(){
		return stateTag;
	}
	
	public int getOwnerId(){
		return ownerId;
	}
	
	public long getData_ts(){
		return data_ts;
	}
	
	public void setData_ts(long data_ts){
		this.data_ts = data_ts;
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
	
	public abstract State[] splitState(State toSplit, int key);
	public abstract int getCounter();
	public abstract void generateBackupState();
	public abstract void installState(State is);
	public abstract long getBackupTime();
	
}
