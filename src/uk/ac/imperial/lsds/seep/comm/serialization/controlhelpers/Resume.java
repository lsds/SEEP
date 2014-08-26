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

public class Resume {

	ArrayList<Integer> opId;

	public Resume(){
		
	}
	
	public Resume(ArrayList<Integer> opId){
		this.opId = opId;
	}
	
	public ArrayList<Integer> getOpId() {
		return opId;
	}

	public void setOpId(ArrayList<Integer> opId) {
		this.opId = opId;
	}
}
