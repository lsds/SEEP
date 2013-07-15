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
package uk.co.imperial.lsds.seep.elastic;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.imperial.lsds.seep.operator.Partitionable;
import uk.co.imperial.lsds.seep.operator.State;
import uk.co.imperial.lsds.seep.processingunit.StreamData;
import uk.co.imperial.lsds.seep.processingunit.StreamStateChunk;

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
	public void setDirtyMode(boolean newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconcile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalNumberOfChunks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public StreamData streamSplitState(State toSplit, int iteration, int key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUpIterator() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StreamData[] getRemainingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void appendChunk(State s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetStructures(int partition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
	}

}
