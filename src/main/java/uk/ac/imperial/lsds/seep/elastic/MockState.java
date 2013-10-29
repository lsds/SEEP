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
package uk.ac.imperial.lsds.seep.elastic;

import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.State;

public class MockState extends State implements Partitionable{

	private static final long serialVersionUID = 1L;

	public MockState(){}
	
	@Override
	public State[] splitState(State toSplit, int key) {
		
		return null;
	}

	@Override
	public String getKeyAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKeyAttribute(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
	}

}
