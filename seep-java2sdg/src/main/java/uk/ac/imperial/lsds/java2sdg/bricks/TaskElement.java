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

import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.SDG.SendType;

public class TaskElement implements SDGElement{

	private final int id;
	// The name of this operator
	private final String opName;
	// The opblock type
	private final TaskElementNature opType;
	// Potential annotation
	private final SDGAnnotation ann;
	// The variables this operator requires locally
	private final List<Variable> localVars;
	// The variables to stream
	private final List<Variable> varsToStream;
	// A map that indicates for each downstream connection how data is sent (record-at-a-time, barrier, etc)
	private SendType sendType;
	// The actual code of this operator
	private final String code;
	
	public int getId(){
		return id;
	}
	
	public String getOpName() {
		return opName;
	}

	public TaskElementNature getOpType() {
		return opType;
	}
	
	public SDGAnnotation getAnn(){
		return ann;
	}

	public List<Variable> getLocalVars() {
		return localVars;
	}
	
	public List<Variable> getVarsToStream() {
		return varsToStream;
	}

//	public Map<String, DataShipmentMode> getConnectionsType() {
//		return connectionsType;
//	}

	public String getCode() {
		return code;
	}
	
	public static class TaskElementBuilder implements SDGElement{
		//mandatory fields
		private final int id;
		private final String opName;
		private TaskElementNature opType;
		//Optional fields
//		private final TaskElementNature opType;
		private SDGAnnotation ann;
		private List<Variable> localVars;
		private List<Variable> varsToStream;
		private String code;
		private StringBuilder sb = new StringBuilder();
		
		public TaskElementBuilder(int id, String name){
			this.id = id;
			this.opName = name;
			this.opType = TaskElementNature.getStatelessTaskElement(); // safe initialisation
		}
		
		public int getId(){
			return id;
		}
		
		public String getOpName(){
			return opName;
		}
		
		public TaskElementBuilder opType(TaskElementNature opType){
			this.opType = opType;
			return this;
		}
		
		public TaskElementNature getOpType(){
			return opType;
		}
		
		public TaskElementBuilder ann(SDGAnnotation ann){
			this.ann = ann;
			return this;
		}
		
		public TaskElementBuilder localVars(List<Variable> val){
			localVars = val;
			return this;
		}
		
		public TaskElementBuilder varsToStream(List<Variable> val){
			varsToStream = val;
			return this;
		}
		
		public void addCodeLine(String val){
			sb.append(val);
			sb.append("\n");
		}
		
		@Deprecated
		public TaskElementBuilder code(String val){
			code = val;
			return this;
		}
		
		public TaskElement build(){
			this.code = sb.toString();
//			if(this.opType == null){
//				log.severe("");
//			}
			return new TaskElement(this);
		}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("TE: "+this.id);
			sb.append("\n");
			sb.append("ANN: "+this.ann);
			sb.append("\n");
			sb.append("TYPE: "+this.opType);
			sb.append("\n");
			if(this.opType != null && this.opType.getStateElementId() != -1){
				sb.append("->SE: "+this.opType.getStateElementId());
				sb.append("\n");
				sb.append("PK: "+this.opType.getPartitioningKey());
				sb.append("\n");
			}
			sb.append(this.sb.toString());
			return sb.toString();
		}
	}
	
	private TaskElement(TaskElementBuilder builder){
		id = builder.id;
		opName = builder.opName;
		opType = builder.opType;
		ann = builder.ann;
		localVars = builder.localVars;
		varsToStream = builder.varsToStream;
		code = builder.code;
	}
	
	public void setSendType(SendType st){
		this.sendType = st;
	}
	
	public SendType getSendType(){
		return sendType;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TE: "+this.id);
		sb.append("\n");
		sb.append("ANN: "+this.ann);
		sb.append("\n");
		sb.append("TYPE: "+this.opType);
		sb.append("\n");
		if(this.opType != null && this.opType.getStateElementId() != -1){
			sb.append("->SE: "+this.opType.getStateElementId());
			sb.append("\n");
			sb.append("PK: "+this.opType.getPartitioningKey());
			sb.append("\n");
		}
		sb.append("TX: "+this.sendType);
		sb.append("\n");
		sb.append("TXtoBranchID: "+this.sendType.getBranchingIdentifier());
		sb.append("\n");
		sb.append("localVars: "+this.localVars);
		sb.append("\n");
		sb.append("varsToStream: "+this.varsToStream);
		sb.append("\n");
		sb.append(this.code);
		return sb.toString();
	}
}
