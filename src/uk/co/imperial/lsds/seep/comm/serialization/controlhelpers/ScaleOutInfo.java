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

public class ScaleOutInfo {
	
	private int oldOpId;
	private int newOpId;
	private boolean isStateful;
	
	public ScaleOutInfo(){
		
	}
	
	public ScaleOutInfo(int oldOpId, int newOpId, boolean isStateful){
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
		this.isStateful = isStateful;
	}
	
	public int getOldOpId() {
		return oldOpId;
	}
	
	public void setOldOpId(int oldOpId) {
		this.oldOpId = oldOpId;
	}
	
	public int getNewOpId() {
		return newOpId;
	}
	
	public void setNewOpId(int newOpId) {
		this.newOpId = newOpId;
	}
	
	public boolean isStatefulScaleOut(){
		return isStateful;
	}
}
