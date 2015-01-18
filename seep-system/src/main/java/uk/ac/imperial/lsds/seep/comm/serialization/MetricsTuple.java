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
package uk.ac.imperial.lsds.seep.comm.serialization;

public class MetricsTuple {

	private int opId;
	
	private long inputQueueEvents;
	private long numberIncomingDataHandlerWorkers;
	
	public MetricsTuple(){
	}

	public int getOpId(){
		return opId;
	}
	
	public long getInputQueueEvents(){
		return inputQueueEvents;
	}
	
	public long getNumberIncomingDataHandlerWorkers(){
		return numberIncomingDataHandlerWorkers;
	}
	
	public void setOpId(int opId){
		this.opId = opId;
	}
	
	public void setInputQueueEvents(long inputQueueEvents) {
		this.inputQueueEvents = inputQueueEvents;		
	}
	
	public void setNumberIncomingDataHandlerWorkers(long numberIncomingdataHandlerWorkers2){
		this.numberIncomingDataHandlerWorkers = numberIncomingdataHandlerWorkers2; 
	}
}
