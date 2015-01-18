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
package uk.ac.imperial.lsds.seep.operator;

import java.io.Serializable;

import uk.ac.imperial.lsds.seep.infrastructure.master.Node;

/**
* Location. Location class models the endpoints of a given operator, providing node and port information.
*/

public class OperatorStaticInformation implements Serializable{

	private static final long serialVersionUID = 1L;

	private int opId;
	private int originalOpId;
	
	private Node myNode;

	private int inC;
	private int inD;
	
	private boolean isStatefull;

	public int getOpId(){
		return opId;
	}
	
	public int getOriginalOpId(){
		return originalOpId;
	}
	
	public Node getMyNode(){
		return myNode;
	}

	public void setMyNode(Node myNode){
		this.myNode = myNode;
	}

	public int getInC(){
		return inC;
	}

	public int getInD(){
		return inD;
	}

	public void setInD(int inD){
		this.inD = inD;
	}
	
	public void setInC(int inC){
		this.inC = inC;
	}
	
	public boolean isStatefull() {
		return isStatefull;
	}

	public void setStatefull(boolean isStatefull) {
		this.isStatefull = isStatefull;
	}

	public OperatorStaticInformation setNode(Node newNode){
		return new OperatorStaticInformation(opId, originalOpId, newNode, inC, inD, isStatefull);
	}
	
	@Override public String toString() {
		return "node: " + myNode + "inC: " + inC + "inD: " + inD;
	}

	public OperatorStaticInformation(int opId, int originalOpId, Node myNode, int inC, int inD, boolean isStatefull){
		this.opId = opId;
		this.originalOpId = originalOpId;
		this.myNode = myNode;
		this.inC = inC;
		this.inD = inD;
		this.isStatefull = isStatefull;
	}
}
