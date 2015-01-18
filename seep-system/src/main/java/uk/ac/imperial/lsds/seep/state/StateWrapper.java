/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.ExtendedObjectInputStream;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.ExtendedObjectOutputStream;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

/**
 * StateWrapper is a class that wraps State types, enriching these with data used by the system.
 * @author raulcf
 *
 */
public class StateWrapper implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
	/** The id of the operator that uses this State **/
	private int ownerId;
	/** A name that represents this state **/
	private String stateTag;
	/** The actual State being wrapped **/
	private State stateImpl;
	/** The {@link TimestampTracker} at the time the last update on the State wrapped by this class **/
	private TimestampTracker data_ts;
	/** The checkpointing interval defined by the user **/
	private int checkpointInterval;
		
	public int getOwnerId(){
		return ownerId;
	}
	
	public void setOwnerId(int ownerId){
		this.ownerId = ownerId;
	}
	
	public String getStateTag(){
		return stateTag;
	}
	
	public void setStateTag(String stateTag){
		this.stateTag = stateTag;
	}
	
	public int getCheckpointInterval(){
		return checkpointInterval;
	}
	
	public void setCheckpointInterval(int checkpointInterval){
		this.checkpointInterval = checkpointInterval;
	}
	
	public TimestampTracker getData_ts(){
		return data_ts;
	}
	
	public void setData_ts(TimestampTracker data_ts){
		this.data_ts = data_ts;
	}
	
	public State getStateImpl(){
		return stateImpl;
	}
	
	public StateWrapper(){
		// Empty constructor for serialization purposes
	}
	
	//TODO by now checkpoints will be performed only temporarily
	public StateWrapper(int ownerId, int checkpointInterval){
		// Mandatory variables to initialize a state
		this.ownerId = ownerId;
		this.checkpointInterval = checkpointInterval;
	}
	
	public StateWrapper(int ownerId, int checkpointInterval, State stateImpl){
		this.ownerId = ownerId;
		this.checkpointInterval = checkpointInterval;
		this.stateImpl = stateImpl;
	}
	
	public StateWrapper clone(){
		try {
			return (StateWrapper) super.clone();
		}
		catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static StateWrapper deepCopy(StateWrapper original, RuntimeClassLoader rcl){
		Object obj = null;
	    try {
	    	// Write the object out to a byte array
	        ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
	        ExtendedObjectOutputStream out = new ExtendedObjectOutputStream(bos);
	        synchronized(original){
	        	out.writeObject(original);
	        	out.flush();
	        	out.close();
	        }
	        // Make an input stream from the byte array and read
	        // a copy of the object back in.
	        byte[] temp = bos.toByteArray();
	        System.out.println("Serialised size: "+temp.length+" bytes");
	        ExtendedObjectInputStream in = new ExtendedObjectInputStream(new ByteArrayInputStream(temp), rcl);
	        obj = in.readObject();
	    }
	    catch(IOException e) {
	    	e.printStackTrace();
	    }
	    catch(ClassNotFoundException cnfe) {
	        cnfe.printStackTrace();
	    }
	    return (StateWrapper) obj;
	}
}
