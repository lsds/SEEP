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
package uk.ac.imperial.lsds.seep.operator;

import java.util.Iterator;

import uk.ac.imperial.lsds.seep.processingunit.StreamData;

public interface Streamable {

	public int getSize();
	public int getTotalNumberOfChunks();
	public void setUpIterator();
	public Iterator getIterator();
	public StreamData streamSplitState(State toSplit, int iteration, int key);
	public StreamData[] getRemainingData();
	public void resetStructures(int partition);
	public void appendChunk(State s);
	
}
