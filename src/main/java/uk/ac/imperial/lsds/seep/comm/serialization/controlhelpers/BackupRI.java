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

import java.util.ArrayList;

public class BackupRI {

	private int opId;
	private String operatorType;
	private ArrayList<Integer> index;
	private ArrayList<Integer> key;
	
	public BackupRI(){
		
	}
	
	public BackupRI(int opId, ArrayList<Integer> index, ArrayList<Integer> key, String operatorType){
		this.opId = opId;
		this.index = index;
		this.key = key;
		this.operatorType = operatorType;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public String getOperatorType() {
		return operatorType;
	}
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}
	public ArrayList<Integer> getIndex() {
		return index;
	}
	public void setIndex(ArrayList<Integer> index) {
		this.index = index;
	}
	public ArrayList<Integer> getKey() {
		return key;
	}
	public void setKey(ArrayList<Integer> key) {
		this.key = key;
	}
	
	
}
