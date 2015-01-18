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
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

public class BackupNodeState {

	private int nodeId;
	private int upstreamOpId;
	private BackupOperatorState[] backupOperatorState;
	
	public int getNodeId(){
		return nodeId;
	}
	
	public int getUpstreamOpId(){
		return upstreamOpId;
	}
	
	public BackupOperatorState[] getBackupOperatorState(){
		return backupOperatorState;
	}
	
	public void setBackupOperatorState(BackupOperatorState[] backupOperatorState){
		this.backupOperatorState = backupOperatorState;
	}
	
	public BackupNodeState(){
		
	}
	
	public BackupNodeState(int nodeId, int upstreamOpId){
		this.nodeId = nodeId;
		this.upstreamOpId = upstreamOpId;
	}
	
	public BackupNodeState(int nodeId, BackupOperatorState[] backupOperatorState){
		this.nodeId = nodeId;
		this.backupOperatorState = backupOperatorState;
	}
	
	public BackupOperatorState getBackupOperatorStateWithOpId(int opId){
		for(int i = 0; i<backupOperatorState.length; i++){
			if(backupOperatorState[i].getOpId() == opId){
				return backupOperatorState[i];
			}
		}
		return null;
	}
	
	public void replaceOpBackupWithId(int opIdToReplace, BackupOperatorState replace){
		for(int i = 0; i<backupOperatorState.length; i++){
			if(backupOperatorState[i].getOpId() == opIdToReplace){
				backupOperatorState[i] = replace;
			}
		}
	}
}
