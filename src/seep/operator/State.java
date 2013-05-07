package seep.operator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import seep.utils.dynamiccodedeployer.ExtendedObjectInputStream;
import seep.utils.dynamiccodedeployer.ExtendedObjectOutputStream;
import seep.utils.dynamiccodedeployer.RuntimeClassLoader;

public abstract class State implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
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
	
//	public State(State toCopy){
//		//This copy-constructor wont be used for anything more than copying
//		this.checkpointInterval = toCopy.checkpointInterval;
//		this.ownerId = toCopy.ownerId;
//		this.stateTag = toCopy.stateTag;
//		this.stateImpl = toCopy.stateImpl;
//		this.data_ts = toCopy.data_ts;
//	}
	
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
	
	public static State deepCopy(State original, RuntimeClassLoader rcl){
		Object obj = null;
	    try {
	    	// Write the object out to a byte array
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ExtendedObjectOutputStream out = new ExtendedObjectOutputStream(bos);
	        synchronized(original){
	        	out.writeObject(original);
	        	out.flush();
	        	out.close();
	        }
	        // Make an input stream from the byte array and read
	        // a copy of the object back in.
	        ExtendedObjectInputStream in = new ExtendedObjectInputStream(new ByteArrayInputStream(bos.toByteArray()), rcl);
	        obj = in.readObject();
	    }
	    catch(IOException e) {
	    	e.printStackTrace();
	    }
	    catch(ClassNotFoundException cnfe) {
	        cnfe.printStackTrace();
	    }
	    return (State) obj;
	}
}
