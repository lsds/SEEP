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
package uk.ac.imperial.lsds.java2sdg.flowanalysis;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;

public class PointOfInterest {
	
	private boolean isPointOfInterest = false;
	private boolean isStateAccess = false;
	
	private SDGAnnotation annotation;
	private int stateElementId;
	private String stateName;
	private int sourceCodeLine;
	private String partitioningKey;

	
	public PointOfInterest(){	
	}
	
	public boolean isStateAccess(){
		return isStateAccess;
	}
	
	public void setSourceCodeLine(int sourceCodeLine){
		this.sourceCodeLine = sourceCodeLine;
	}
	
	public int getSourceCodeLine(){
		return sourceCodeLine;
	}
	
	public void setAnnotation(SDGAnnotation ann){
		if(ann != null){
			this.annotation = ann;
			this.isPointOfInterest = true;
		}
		else{
			this.annotation = null;
		}
	}
	
	public boolean isPointOfInterest(){
		return isPointOfInterest;
	}
	
	public SDGAnnotation getAnnotation(){
		return annotation;
	}
	
	public void setStateElementId(int id){
		this.isPointOfInterest = true;
		this.isStateAccess = true;
		this.stateElementId = id;
	}
	
	public int getStateElementId(){
		return stateElementId;
	}
	
	public void setStateName(String name){
		this.isStateAccess = true;
		this.stateName = name;
	}
	
	public String getStateName(){
		return stateName;
	}
	
	public void setPartitioningKey(String key){
		this.partitioningKey = key;
	}
	
	public String getPartitioningKey(){
		return partitioningKey;
	}
	
	@Deprecated
	public boolean isSameStateAccess(PointOfInterest sa){
		System.out.println("Comparing: "+sa.stateElementId+" with: "+this.stateElementId);
		return sa.stateElementId == (this.stateElementId) && sa.sourceCodeLine != this.sourceCodeLine;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(isPointOfInterest){
			sb.append("LINE: "+sourceCodeLine);
			sb.append("\n");
			sb.append("ANNOTATION: "+annotation);
			sb.append("\n");
			sb.append("STATE ID: "+stateElementId+" STATE NAME: "+stateName);
			sb.append("\n");
			sb.append("P_KEY: "+partitioningKey);
			sb.append("\n");
		}
		else{
			sb.append("No POI");
		}
		return sb.toString();
	}
	
}
