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

public final class Stream {

	private final int id;
	private final int workflowId;
	private final StreamType type;
	
	public Stream(int id, int workflowId, StreamType type){
		this.id = id;
		this.workflowId = workflowId;
		this.type = type;
	}
	
	public int getId(){
		return id;
	}
	
	public int getWorkflowId(){
		return workflowId;
	}
	
	public StreamType getType(){
		return type;
	}
}
