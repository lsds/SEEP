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
package uk.ac.imperial.lsds.seep.api;

import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;

public class ScaleOutIntentBean {

	private Operator opToScaleOut;
	private int newOpId;
	private Node newProvisionedNode;
	private Operator newInstantiation;
	
	public Operator getOpToScaleOut() {
		return opToScaleOut;
	}

	public void setOpToScaleOut(Operator opToScaleOut) {
		this.opToScaleOut = opToScaleOut;
	}

	public int getNewOpId() {
		return newOpId;
	}

	public void setNewOpId(int newOpId) {
		this.newOpId = newOpId;
	}

	public Node getNewProvisionedNode() {
		return newProvisionedNode;
	}

	public void setNewProvisionedNode(Node newProvisionedNode) {
		this.newProvisionedNode = newProvisionedNode;
	}
	
	public Operator getNewOperatorInstantiation(){
		return newInstantiation;
	}
	
	public void setNewReplicaInstantiation(Operator newInstantiation){
		this.newInstantiation = newInstantiation;
	}
	
	public ScaleOutIntentBean(Operator opToScaleOut, int newOpId, Node newProvisionedNode){
		this.opToScaleOut = opToScaleOut;
		this.newOpId = newOpId;
		this.newProvisionedNode = newProvisionedNode;
	}
	
	@Override
	public String toString(){
		return "OP: "+opToScaleOut.getOperatorId()+" scales to new OP-id: "+newOpId;
	}
	
}
