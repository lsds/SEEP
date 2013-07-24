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
package uk.co.imperial.lsds.seep.operator;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.imperial.lsds.seep.processingunit.StreamData;

public interface Partitionable {

	public void setDirtyMode(boolean newValue);
	public void reconcile();
	public int getSize();
	public void setKeyAttribute(String keyAttribute);
	public String getKeyAttribute();
	public State[] splitState(State toSplit, int key);
	public StreamData streamSplitState(State toSplit, int iteration, int key);
	public int getTotalNumberOfChunks();
	public void setUpIterator();
	public StreamData[] getRemainingData();
	public Iterator getIterator();
	public void appendChunk(State s);
	public void resetStructures(int partition);
	public void resetState();
	
}
