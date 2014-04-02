/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.java2sdg.bricks;

public class TaskElementNature {
	
	public enum Nature{
		STATELESS_SOURCE, STATEFUL_SOURCE,
		STATELESS_SINK, STATEFUL_SINK,
		STATELESS_OPERATOR, STATEFUL_OPERATOR;
	}
	
	private final int stateElementId;
	private final String stateName;
	private final String partitioningKey;
	private final Nature nature;
	
	public static TaskElementNature getStatefulTaskElement(
			int stateElementId, String stateName, String partitioningKey){
		return new TaskElementNature(stateElementId, stateName, partitioningKey, Nature.STATEFUL_OPERATOR);
	}
	
	public static TaskElementNature getStatelessTaskElementWithStateReference(int stateId){
		return new TaskElementNature(stateId, "null", "null", Nature.STATELESS_OPERATOR);
	}
	
	public static TaskElementNature getStatelessTaskElement(){
		return new TaskElementNature(-1, "null", "null", Nature.STATELESS_OPERATOR);
	}
	
	public static TaskElementNature getStatelessSource(){
		return new TaskElementNature(-1, "null", "null", Nature.STATELESS_SOURCE);
	}
	
	public static TaskElementNature getStatelessSink(){
		return new TaskElementNature(-1, "null", "null", Nature.STATELESS_SINK);
	}
	
	public Nature getNature(){
		return nature;
	}
	
	private TaskElementNature(int stateElementId, String stateName, String partitioningKey, Nature nature){
		this.stateElementId = stateElementId;
		this.stateName = stateName;
		this.partitioningKey = partitioningKey;
		this.nature = nature;
	}
	
	public int getStateElementId(){
		return stateElementId;
	}
	
	public String getStateName(){
		return stateName;
	}
	
	public String getPartitioningKey(){
		return partitioningKey;
	}
	
	public String toString(){
		return this.nature.toString();
	}
}
