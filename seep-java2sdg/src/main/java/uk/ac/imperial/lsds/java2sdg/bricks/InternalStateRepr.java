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
package uk.ac.imperial.lsds.java2sdg.bricks;

import soot.SootClass;

public class InternalStateRepr {

	public enum StateLabel{
		PARTITIONED, PARTIAL
	}
	
	private final int seId;
	private final SootClass stateClass;
	private final StateLabel stateLabel;
	
	public InternalStateRepr(SootClass stateClass, StateLabel stateLabel, int seId){
		this.seId = seId;
		this.stateClass = stateClass;
		this.stateLabel = stateLabel;
	}
	
	public int getSeId(){
		return seId;
	}
	
	public SootClass getStateClass(){
		return stateClass;
	}
	
	public StateLabel getStateLabel(){
		return stateLabel;
	}
}
