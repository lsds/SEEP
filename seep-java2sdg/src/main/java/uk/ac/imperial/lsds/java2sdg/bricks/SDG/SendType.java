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
package uk.ac.imperial.lsds.java2sdg.bricks.SDG;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElementNature;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElementNature.Nature;

public enum SendType {
	SEND, SEND_ALL, SEND_KEY, SEND_STREAMID, SEND_STREAMID_KEY;
	
	// This special argument gets translated into a variable that indicates the tuple scheme, used by downstream nodes to know how to process it
	private int branchingIdentifier = -1;
	// All variables related to state, stateId, stateName and most importantly, partitioningKey required in case of stateful operators
	private int stateElementId;
	private String stateName;
	private String partitioningKey;
	
	public void setBranchingIdentifier(int branchingIdentifier){
		this.branchingIdentifier = branchingIdentifier;
	}
	
	public int getBranchingIdentifier(){
		return branchingIdentifier;
	}
	
	public static SendType getSendType(SDGAnnotation ann, TaskElementNature opType){
		if(ann == SDGAnnotation.COLLECTION){
			return SendType.SEND;
		}
		else if(ann == SDGAnnotation.GLOBAL_READ || ann == SDGAnnotation.GLOBAL_WRITE){
			return SendType.SEND_ALL;
		}
		else if(opType.getNature() == Nature.STATEFUL_OPERATOR){
			SendType st = SendType.SEND_KEY;
			st.partitioningKey = opType.getPartitioningKey();
			st.stateElementId = opType.getStateElementId();
			st.stateName = opType.getStateName();
			return st;
		}
		else if(opType.getNature() == Nature.STATELESS_OPERATOR){
			return SendType.SEND;
		}
		// kind of a safe default
		return SendType.SEND;
	}
}

