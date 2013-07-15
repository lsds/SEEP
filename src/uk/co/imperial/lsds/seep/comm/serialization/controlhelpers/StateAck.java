/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

public class StateAck {

	private int nodeId;
	private int mostUpstreamOpId;

	public StateAck(){
		
	}
	
	public StateAck(int nodeId, int mostUpstreamOpId){
		this.nodeId = nodeId;
		this.mostUpstreamOpId = mostUpstreamOpId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public int getMostUpstreamOpId(){
		return mostUpstreamOpId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public void setMostUpstreamOpId(int mostUpstreamOpId){
		this.mostUpstreamOpId = mostUpstreamOpId;
	}
}
