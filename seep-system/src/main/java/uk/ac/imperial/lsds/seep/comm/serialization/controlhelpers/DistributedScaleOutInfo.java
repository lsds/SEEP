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

public class DistributedScaleOutInfo {
		
	private int oldOpId;
	private int newOpId;
		
	public DistributedScaleOutInfo(){
			
	}
		
	public DistributedScaleOutInfo(int oldOpId, int newOpId){
		this.oldOpId = oldOpId;
		this.newOpId = newOpId;
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
}
